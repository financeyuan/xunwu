package com.yaoyao.online.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.yaoyao.online.DTO.HouseBucketDTO;
import com.yaoyao.online.base.*;
import com.yaoyao.online.entity.House;
import com.yaoyao.online.entity.HouseDetail;
import com.yaoyao.online.entity.HouseTag;
import com.yaoyao.online.entity.SupportAddress;
import com.yaoyao.online.repository.HourseTagRepository;
import com.yaoyao.online.repository.HouseDetailRepository;
import com.yaoyao.online.repository.HouseRepository;
import com.yaoyao.online.repository.SupportAddressRepository;
import com.yaoyao.online.service.HouseIndexMessage;
import com.yaoyao.online.service.HouseIndexTemplate;
import com.yaoyao.online.service.IAddressService;
import com.yaoyao.online.service.ISearchService;
import com.yaoyao.online.web.Form.BaiduMapLocation;
import com.yaoyao.online.web.Form.HouseSuggest;
import com.yaoyao.online.web.Form.MapSearch;
import com.yaoyao.online.web.Form.RentSearch;
import jdk.internal.org.objectweb.asm.tree.analysis.Analyzer;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequestBuilder;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.delete.DeleteAction;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedSingleValueNumericMetricsAggregation;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/13 10:56
 * @Description:
 */
@Service
public class SearchServiceImpl implements ISearchService {

    private static final Logger logger = LoggerFactory.getLogger(ISearchService.class);

    private static final String INDEX_NAME = "xunwu";

    private static final String INDEX_TYPE = "house";

    private static final String INDEX_QUNEN = "topic.message";


    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TransportClient esClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HouseDetailRepository houseDetailRepository;

    @Autowired
    private SupportAddressRepository supportAddressRepository;

    @Autowired
    private HourseTagRepository hourseTagRepository;

    @Autowired
    private IAddressService addressService;

    @Autowired
    private AmqpTemplate template;


    @RabbitListener(queues = INDEX_QUNEN)
    private void handleMessage(String content) throws IOException {

        //HouseIndexMessage message = objectMapper.readValue(content, HouseIndexMessage.class);
        HouseIndexMessage message = JSON.parseObject(content, HouseIndexMessage.class);
        switch (message.getOperation()) {
            case HouseIndexMessage.INDEX:
                this.createOrUpdateIndex(message);
                break;
            case HouseIndexMessage.REMOVE:
                this.removeIndex(message);
                break;
            default:
                logger.warn("Not support message content " + content);
                break;
        }

    }

    private void createOrUpdateIndex(HouseIndexMessage message) {
        Long houseId = message.getHouseId();
        House house = houseRepository.findOne(houseId);
        if (house == null) {
            logger.error("Index house {} dose not exist!", houseId);
            this.index(houseId, message.getRetry() + 1);
            return;
        }
        HouseIndexTemplate indexTemplate = new HouseIndexTemplate();
        modelMapper.map(house, indexTemplate);
        HouseDetail detail = houseDetailRepository.findByHouseId(houseId);
        if (detail == null) {
            // TODO 异常情况
        }
        modelMapper.map(detail, indexTemplate);
        //获取具体地址
        SupportAddress city = supportAddressRepository.findByEnNameAndLevel(house.getCityEnName()
                , SupportAddress.Level.CITY.getValue());
        SupportAddress region = supportAddressRepository.findByEnNameAndLevel(house
                .getRegionEnName(), SupportAddress.Level.REGION.getValue());
        String address = city.getCnName() + region.getCnName() + house.getStreet() + house
                .getDistrict() + detail.getDetailAddress();

        ServiceResult<BaiduMapLocation> mapLocation = addressService.getBaiduMapLocation
                (city.getCnName(), address);
        if (!mapLocation.isSuccess()) {
            this.index(message.getHouseId(), message.getRetry() + 1);
        }
        indexTemplate.setBaiduMapLocation(mapLocation.getResult());

        List<HouseTag> tags = hourseTagRepository.findAllByHouseId(houseId);
        if (tags != null && !tags.isEmpty()) {
            List<String> tagStrings = new ArrayList<>();
            tags.forEach(houseTag -> tagStrings.add(houseTag.getName()));
            indexTemplate.setTags(tagStrings);
        }
        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(INDEX_NAME).setTypes
                (INDEX_TYPE)
                .setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId));
        logger.debug(requestBuilder.toString());
        SearchResponse searchResponse = requestBuilder.get();
        boolean tag;
        long totalHit = searchResponse.getHits().getTotalHits();
        if (totalHit == 0) {
            tag = create(indexTemplate);
        } else if (totalHit == 1) {
            String esId = searchResponse.getHits().getAt(0).getId();
            tag=update(indexTemplate, esId);
        } else {
            tag=deleteAndCreate(totalHit, indexTemplate);
        }
        ServiceResult result = addressService.lbsUpload(mapLocation.getResult(), house.getStreet
                        () + house.getDistrict(),
                city.getCnName() + region.getCnName() + house.getStreet() + house.getDistrict(),
                house
                        .getId(), house.getPrice(), house.getArea());
        if(!tag || !result.isSuccess()){
            this.index(message.getHouseId(),message.getRetry()+1);
        }

    }

    @Override
    public void index(Long houseId) {
        this.index(houseId, 0);
    }

    private void index(Long houseId, int retry) {
        if (retry > HouseIndexMessage.MAX_RETRY) {
            logger.error("Retry index times over 3 for house: " + houseId + " Please check it!");
            return;
        }

        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.INDEX, retry);
        try {
            this.template.convertAndSend(INDEX_QUNEN, JSON.toJSONString(message));
        } catch (Exception e) {
            logger.error("Error for " + e.getMessage());
        }

    }

    private boolean create(HouseIndexTemplate indexTemplate) {
        if (!updateSuggest(indexTemplate)) {
            return false;
        }
        try {
            IndexResponse indexResponse = this.esClient.prepareIndex(INDEX_NAME, INDEX_TYPE)
                    .setSource(objectMapper
                            .writeValueAsBytes(indexTemplate), XContentType.JSON).get();
            if (indexResponse.status() == RestStatus.CREATED) {
                return true;
            }
            return false;
        } catch (JsonProcessingException e) {
            logger.error("Error to create Index with houseId:" + indexTemplate.getHouseId(), e
                    .getMessage());
            return false;
        }

    }

    private boolean update(HouseIndexTemplate indexTemplate, String edId) {
        if (!updateSuggest(indexTemplate)) {
            return false;
        }
        try {
            UpdateResponse updateResponse = this.esClient.prepareUpdate(INDEX_NAME, INDEX_TYPE,
                    edId)
                    .setDoc(objectMapper
                            .writeValueAsBytes(indexTemplate), XContentType.JSON).get();
            if (updateResponse.status() == RestStatus.OK) {
                return true;
            }
            return false;
        } catch (JsonProcessingException e) {
            logger.error("Error to create Index with houseId:" + indexTemplate.getHouseId(), e
                    .getMessage());
            return false;
        }
    }

    private boolean deleteAndcreate(HouseIndexTemplate indexTemplate, Long totalHit) {
        DeleteByQueryRequestBuilder source = DeleteByQueryAction.INSTANCE.newRequestBuilder
                (esClient).filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID,
                indexTemplate.getHouseId()))
                .source(INDEX_NAME);
        logger.debug("Delete By Query for house: " + source);
        BulkByScrollResponse bulkByScrollResponse = source.get();
        long deleted = bulkByScrollResponse.getDeleted();
        if (deleted != totalHit) {
            logger.warn("Need delete {},but {} was deleted", totalHit, deleted);
            return false;
        }
        return create(indexTemplate);
    }

    @Override
    public void remove(Long houseId) {
        this.remove(houseId, 0);
    }

    @Override
    public ServiceMultiResult<Long> query(RentSearch rentSearch) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, rentSearch
                .getCityEnName()));
        if (rentSearch.getRegionEnName() != null && !"*".equals(rentSearch.getRegionEnName())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME,
                    rentSearch.getRegionEnName()));
        }

        /**
         * 面积查询
         */
        RentValueBlock rentValueBlock = RentValueBlock.matchArea(rentSearch.getAreaBlock());
        if (!RentValueBlock.ALL.equals(rentValueBlock)) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(HouseIndexKey.AREA);
            if (rentValueBlock.getMax() > 0) {
                rangeQueryBuilder.lte(rentValueBlock.getMax());
            }
            if (rentValueBlock.getMin() > 0) {
                rangeQueryBuilder.gte(rentValueBlock.getMin());
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        /**
         * 价格查询
         */
        RentValueBlock price = RentValueBlock.matchPrice(rentSearch.getPriceBlock());
        if (!RentValueBlock.ALL.equals(price)) {
            RangeQueryBuilder rangeQueryPriceBuilder = QueryBuilders.rangeQuery(HouseIndexKey
                    .PRICE);
            if (price.getMax() > 0) {
                rangeQueryPriceBuilder.lte(price.getMax());
            }
            if (price.getMin() > 0) {
                rangeQueryPriceBuilder.gte(price.getMin());
            }
            boolQueryBuilder.filter(rangeQueryPriceBuilder);
        }

        /**
         * 方向查询
         */
        if (rentSearch.getDirection() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.DESCRIPTION, rentSearch
                    .getDirection()));
        }

        /**
         * 租住方式查询
         */
        if (rentSearch.getRentWay() > -1) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.RENT_WAY, rentSearch
                    .getRentWay()));
        }
        /**
         * 关键字查询
         */
//        boolQueryBuilder.must(QueryBuilders.matchQuery(HouseIndexKey.TITLE,rentSearch.getKeywords
//                ()).boost(2.0f));
        boolQueryBuilder.should(QueryBuilders.multiMatchQuery(rentSearch.getKeywords(),
                HouseIndexKey.TITLE, HouseIndexKey.TRAFFIC, HouseIndexKey.DISTRICT, HouseIndexKey
                        .ROUND_SERVICE, HouseIndexKey.SUBWAY_STATION_NAME, HouseIndexKey
                        .SUBWAY_LINE_NAME));

        SearchRequestBuilder searchRequestBuilder = this.esClient.prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE).setQuery(boolQueryBuilder).addSort(
                        HouseSort.getSortKey(rentSearch.getOrderBy()),
                        SortOrder.fromString(rentSearch.getOrderDirection())
                ).setFrom(rentSearch.getStart()).setSize(rentSearch.getSize()).setFetchSource
                        (HouseIndexKey.HOUSE_ID, null);
        logger.debug(searchRequestBuilder.toString());
        List<Long> houseIds = new ArrayList<>();
        SearchResponse searchResponse = searchRequestBuilder.get();
        if (searchResponse.status() != RestStatus.OK) {
            logger.error(searchRequestBuilder + "is error");
            return new ServiceMultiResult<>(0, houseIds);
        }
        for (SearchHit hit : searchResponse.getHits()) {
            logger.debug(hit.getSourceAsMap().toString());
            houseIds.add(Longs.tryParse(String.valueOf(hit.getSourceAsMap().get(HouseIndexKey
                    .HOUSE_ID))));
        }
        return new ServiceMultiResult<>(searchResponse.getHits().totalHits, houseIds);
    }

    @Override
    public ServiceResult<List<String>> suggest(String prefix) {
        CompletionSuggestionBuilder suggestion = SuggestBuilders.completionSuggestion("suggest")
                .prefix(prefix).size(10);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("autocomplete", suggestion);
        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(INDEX_NAME).setTypes
                (INDEX_TYPE)
                .suggest(suggestBuilder);
        logger.debug(requestBuilder.toString());
        SearchResponse response = requestBuilder.get();
        Suggest suggest = response.getSuggest();
        Suggest.Suggestion result = suggest.getSuggestion("autocomplete");
        int maxSuggest = 0;
        Set<String> suggestSet = new HashSet<String>();
        for (Object term : result.getEntries()) {
            if (term instanceof CompletionSuggestion.Entry) {
                CompletionSuggestion.Entry item = (CompletionSuggestion.Entry) term;
                if (item.getOptions().isEmpty()) {
                    continue;
                }
                for (CompletionSuggestion.Entry.Option option : item.getOptions()) {
                    String tip = option.getText().string();
                    if (suggestSet.contains(tip)) {
                        continue;
                    }
                    suggestSet.add(tip);
                    maxSuggest++;
                }
            }
            if (maxSuggest > 5) {
                break;
            }
        }
        List<String> suggests = Lists.newArrayList(suggestSet.toArray(new String[]{}));
        return ServiceResult.of(suggests);
    }

    @Override
    public ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName,
                                                      String district) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().filter(QueryBuilders
                .termQuery(HouseIndexKey.CITY_EN_NAME, cityEnName)).filter(QueryBuilders.termQuery
                (HouseIndexKey.REGION_EN_NAME, regionEnName)).filter(QueryBuilders.termQuery
                (HouseIndexKey.DISTRICT, district));
        SearchRequestBuilder searchRequestBuilder = this.esClient.prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE).setQuery(boolQueryBuilder).addAggregation(
                        AggregationBuilders.terms(HouseIndexKey.AGG_DISTRICT).field(HouseIndexKey
                                .DISTRICT)
                ).setSize(0);
        logger.debug(boolQueryBuilder.toString());
        SearchResponse searchResponse = searchRequestBuilder.get();
        if (searchResponse.status() == RestStatus.OK) {
            Terms terms = searchResponse.getAggregations().get(HouseIndexKey.AGG_DISTRICT);
            if (terms.getBuckets() != null && !terms.getBuckets().isEmpty()) {
                return ServiceResult.of(terms.getBucketByKey(district).getDocCount());
            }
        } else {
            logger.error("error");
        }
        return ServiceResult.of(0L);
    }

    @Override
    public ServiceMultiResult<HouseBucketDTO> mapAggregate(String cityEnName) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, cityEnName));
        TermsAggregationBuilder field = AggregationBuilders.terms(HouseIndexKey.AGG_REGION).field
                (HouseIndexKey.REGION_EN_NAME);
        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE).setQuery(boolQueryBuilder)
                .addAggregation(field);
        logger.debug(requestBuilder.toString());
        SearchResponse searchResponse = requestBuilder.get();
        List<HouseBucketDTO> buckets = new ArrayList<>();
        if (searchResponse.status() != RestStatus.OK) {
            logger.warn("Aggregate status is not ok for " + requestBuilder);
            return new ServiceMultiResult<>(0, buckets);
        }
        Terms terms = searchResponse.getAggregations().get(HouseIndexKey.AGG_REGION);
        for (Terms.Bucket bucket : terms.getBuckets()) {
            buckets.add(new HouseBucketDTO(bucket.getKeyAsString(), bucket.getDocCount()));
        }
        return new ServiceMultiResult<>(searchResponse.getHits().getTotalHits(), buckets);
    }

    @Override
    public ServiceMultiResult<Long> mapQuery(String cityEnName, String orderby, String
            orderDirection, int start, int size) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, cityEnName));
        SearchRequestBuilder searchRequestBuilder = this.esClient.prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE).setQuery(boolQueryBuilder)
                .addSort(HouseSort.getSortKey(orderby), SortOrder.fromString(orderDirection))
                .setFrom(start).setSize(size);
        List<Long> houseIds = new ArrayList<>();
        SearchResponse searchResponse = searchRequestBuilder.get();
        if (searchResponse.status() != RestStatus.OK) {
            logger.warn("Search status is not ok for " + searchRequestBuilder);
            return new ServiceMultiResult<>(0, new ArrayList<>());
        }
        for (SearchHit documentFields : searchResponse.getHits()) {
            houseIds.add(Longs.tryParse(String.valueOf(documentFields.getSourceAsMap().get
                    (HouseIndexKey
                    .HOUSE_ID))));
        }
        return new ServiceMultiResult<>(searchResponse.getHits().getTotalHits(), houseIds);
    }

    @Override
    public ServiceMultiResult<Long> mapQuery(MapSearch mapSearch) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME,mapSearch
                .getCityEnName()));
        boolQueryBuilder.filter(
          QueryBuilders.geoBoundingBoxQuery("location").setCorners(
                  new GeoPoint(mapSearch.getLeftLatitude(),mapSearch.getLeftLongitude()),
                  new GeoPoint(mapSearch.getRightLatitude(),mapSearch.getRightLongitude())
          )
        );
        List<Long> houseIds = new ArrayList<>();
        SearchRequestBuilder searchRequestBuilder = this.esClient.prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE).setQuery(boolQueryBuilder)
                .addSort(HouseSort.getSortKey(mapSearch.getOrderBy()), SortOrder.fromString
                        (mapSearch.getOrderDirection())).setSize(mapSearch.getSize()).setFrom
                        (mapSearch.getStart());
        SearchResponse searchResponse = searchRequestBuilder.get();
        if(searchResponse.status() != RestStatus.OK){
            logger.warn("search is not ok for "+searchRequestBuilder);
            return new ServiceMultiResult<>(0,new ArrayList<>());
        }
        for (SearchHit documentFields : searchResponse.getHits()) {
            houseIds.add(Longs.tryParse(String.valueOf(documentFields.getSourceAsMap().get
                    (HouseIndexKey.HOUSE_ID))));
        }
        return new ServiceMultiResult<>(searchResponse.getHits().getTotalHits(),houseIds);
    }

    private boolean updateSuggest(HouseIndexTemplate indexTemplate) {
        AnalyzeRequestBuilder requestBuilder = new AnalyzeRequestBuilder(
                this.esClient, AnalyzeAction.INSTANCE, INDEX_NAME, indexTemplate.getTitle(),
                indexTemplate.getLayoutDesc(), indexTemplate.getRoundService(),
                indexTemplate.getDescription(), indexTemplate.getSubwayLineName(),
                indexTemplate.getSubwayStationName());

        requestBuilder.setAnalyzer("ik_smart");
        AnalyzeResponse response = requestBuilder.get();
        List<AnalyzeResponse.AnalyzeToken> tokens = response.getTokens();
        if (tokens == null) {
            logger.warn("Can not analyze token for house: " + indexTemplate.getHouseId());
            return false;
        }

        List<HouseSuggest> suggests = new ArrayList<>();
        for (AnalyzeResponse.AnalyzeToken token : tokens) {
            // 排序数字类型 & 小于2个字符的分词结果
            if ("<NUM>".equals(token.getType()) || token.getTerm().length() < 2) {
                continue;
            }
            HouseSuggest suggest = new HouseSuggest();
            suggest.setInput(token.getTerm());
            suggests.add(suggest);
        }

        // 定制化数据自动补全
//        HouseSuggest suggest = new HouseSuggest();
//        suggest.setInput(indexTemplate.getDistrict());
//        suggests.add(suggest);

        indexTemplate.setSuggests(suggests);
        return true;
    }

    private void remove(Long houseId, int retry) {
        if (retry > HouseIndexMessage.MAX_RETRY) {
            logger.error("Retry remove times over 3 for house: " + houseId + " Please check it!");
            return;
        }

        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.REMOVE, retry);
        try {
            this.template.convertAndSend(INDEX_QUNEN, JSON.toJSONString(message));
        } catch (Exception e) {
            logger.error("Cannot encode json for " + message, e);
        }
    }

    private void removeIndex(HouseIndexMessage message) {
        Long houseId = message.getHouseId();
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(esClient)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId))
                .source(INDEX_NAME);
        logger.debug("Delete by query for house: " + builder);
        BulkByScrollResponse response = builder.get();
        ServiceResult result = addressService.removeLbs(houseId);
        long deleted = response.getDeleted();
        logger.debug("Delete total " + deleted);
        if(!result.isSuccess() || deleted <= 0){
            this.remove(houseId, message.getRetry() + 1);
        }
    }

    private boolean deleteAndCreate(long totalHit, HouseIndexTemplate indexTemplate) {
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(esClient)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, indexTemplate.getHouseId()))
                .source(INDEX_NAME);

        logger.debug("Delete by query for house: " + builder);

        BulkByScrollResponse response = builder.get();
        long deleted = response.getDeleted();
        if (deleted != totalHit) {
            logger.warn("Need delete {}, but {} was deleted!", totalHit, deleted);
            return false;
        } else {
            return create(indexTemplate);
        }
    }

}
