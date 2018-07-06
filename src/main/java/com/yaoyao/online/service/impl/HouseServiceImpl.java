package com.yaoyao.online.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.yaoyao.online.DTO.HouseDTO;
import com.yaoyao.online.DTO.HouseDetailDTO;
import com.yaoyao.online.DTO.HousePictureDTO;
import com.yaoyao.online.DTO.HouseSubscribeDTO;
import com.yaoyao.online.base.*;
import com.yaoyao.online.entity.*;
import com.yaoyao.online.repository.*;
import com.yaoyao.online.service.IHouseService;
import com.yaoyao.online.service.IQiNiuService;
import com.yaoyao.online.service.ISearchService;
import com.yaoyao.online.web.Form.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.*;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 14:28
 * @Description:
 */

@Service
public class HouseServiceImpl implements IHouseService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private SubwayRepository subwayRepository;

    @Autowired
    private SubwayStationRepository subwayStationRepository;

    @Autowired
    private HourseTagRepository houseTagRepository;


    @Autowired
    private HouseSubscribeRepository subscribeRespository;

    @Autowired
    private HouseDetailRepository houseDetailRepository;

    @Autowired
    private HousePictureRepository housePictureRepository;

    @Autowired
    private IQiNiuService qiNiuService;

    @Value("${qunniu.cdn.prefix}")
    private String cdnPrefix;

    @Autowired
    private ISearchService searchService;


    @Override
    public ServiceResult<HouseDTO> save(HouseForm houseForm) {
        HouseDetail detail = new HouseDetail();
        ServiceResult<HouseDTO> subwayValidtionResult = wrapperDetailInfo(detail, houseForm);
        if (subwayValidtionResult != null) {
            return subwayValidtionResult;
        }

        House house = new House();
        modelMapper.map(houseForm, house);

        Date now = new Date();
        house.setCreateTime(now);
        house.setLastUpdateTime(now);
        house.setAdminId(LoginUserUtil.getLoginUserId());
        house = houseRepository.save(house);

        detail.setHouseId(house.getId());
        detail = houseDetailRepository.save(detail);

        List<HousePicture> pictures = generatePictures(houseForm, house.getId());
        Iterable<HousePicture> housePictures = housePictureRepository.save(pictures);

        HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
        HouseDetailDTO houseDetailDTO = modelMapper.map(detail, HouseDetailDTO.class);

        houseDTO.setHouseDetail(houseDetailDTO);

        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        housePictures.forEach(housePicture -> pictureDTOS.add(modelMapper.map(housePicture,
                HousePictureDTO.class)));
        houseDTO.setPictures(pictureDTOS);
        houseDTO.setCover(houseDTO.getCover());
        houseDTO.setCover(this.cdnPrefix + houseDTO.getCover());

        List<String> tags = houseForm.getTags();
        if (tags != null && !tags.isEmpty()) {
            List<HouseTag> houseTags = new ArrayList<>();
            for (String tag : tags) {
                houseTags.add(new HouseTag(house.getId(), tag));
            }
            houseTagRepository.save(houseTags);
            houseDTO.setTags(tags);
        }

        return new ServiceResult<HouseDTO>(true, null, houseDTO);
    }

    @Override
    public ServiceMultiResult<HouseDTO> adminQuery(DataTableSearch searchBody) {
        List<HouseDTO> houseDTOS = new ArrayList<>();
        Sort sort = new Sort(Sort.Direction.fromString(searchBody.getDirection()), searchBody
                .getOrderBy());
        int page = searchBody.getStart() / searchBody.getLength();
        Pageable pageable = new PageRequest(page, searchBody.getLength(), sort);
        Specification<House> specification = (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("adminId"), LoginUserUtil.getLoginUserId());
            predicate = cb.and(predicate, cb.notEqual(root.get("status"), HouseStatus.DELETED
                    .getValue()));
            if (searchBody.getCity() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("cityEnName"), searchBody.getCity
                        ()));
            }
            if (searchBody.getStatus() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), searchBody.getStatus()));
            }
            if (searchBody.getCreateTimeMin() != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createTime"),
                        searchBody.getCreateTimeMin()));
            }
            if (searchBody.getGetCreateTimeMax() != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createTime"),
                        searchBody.getGetCreateTimeMax()));
            }
            if (searchBody.getTitle() != null) {
                predicate = cb.and(predicate, cb.like(root.get("title"), "%" + searchBody.getTitle()
                        + "%"));
            }
            return predicate;
        };
        Page<House> housePage = houseRepository.findAll(specification, pageable);

        housePage.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(house.getCover());
            houseDTO.setCover(this.cdnPrefix + house.getCover());
            houseDTOS.add(houseDTO);
        });

        return new ServiceMultiResult(housePage.getTotalElements(), houseDTOS);
    }

    @Override
    public ServiceResult<HouseDTO> findCompleteOne(Long id) {
        House house = houseRepository.findOne(id);
        if (house == null) {
            return ServiceResult.notFound();
        }
        HouseDetail houseDetail = houseDetailRepository.findByHouseId(id);
        List<HouseTag> houseTags = houseTagRepository.findAllByHouseId(id);
        List<HousePicture> housePictures = housePictureRepository.findAllByHouseId(id);
        HouseDetailDTO houseDetailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);
        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        housePictures.forEach(housePicture -> {
            pictureDTOS.add(modelMapper.map(housePicture, HousePictureDTO.class));
        });
        List<String> tags = new ArrayList<>();
        houseTags.forEach(houseTag -> {
            tags.add(houseTag.getName());
        });
        HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
        houseDTO.setHouseDetail(houseDetailDTO);
        houseDTO.setPictures(pictureDTOS);
        houseDTO.setTags(tags);
        //已登录用户
        if (LoginUserUtil.getLoginUserId() > 0) {
            HouseSubscribe houseSubscribe = subscribeRespository.findByHouseIdAndUserId(house
                    .getId(), LoginUserUtil.getLoginUserId());
            if (houseSubscribe != null) {
                houseDTO.setStatus(houseSubscribe.getStatus());
            }
        }
        return ServiceResult.of(houseDTO);
    }

    @Override
    @Transactional
    public ServiceResult Update(HouseForm houseForm) {
        House house = this.houseRepository.findOne(houseForm.getId());
        if (house == null) {
            return ServiceResult.notFound();
        }
        HouseDetail houseDetail = this.houseDetailRepository.findByHouseId(houseForm.getId());
        if (houseDetail == null) {
            return ServiceResult.notFound();
        }
        ServiceResult wrapperResult = wrapperDetailInfo(houseDetail, houseForm);
        if (wrapperResult != null) {
            return wrapperResult;
        }
        this.houseDetailRepository.save(houseDetail);

        List<HousePicture> housePictures = generatePictures(houseForm, houseForm.getId());
        this.housePictureRepository.save(housePictures);
        if (houseForm.getCover() == null) {
            houseForm.setCover(house.getCover());
        }
        modelMapper.map(houseForm, house);
        house.setLastUpdateTime(new Date());
        houseRepository.save(house);

        return ServiceResult.success();
    }

    @Override
    public ServiceResult removePhoto(Long id) {
        HousePicture housePicture = housePictureRepository.findOne(id);
        if (housePicture != null) {
            return ServiceResult.notFound();
        }
        try {
            Response reponse = this.qiNiuService.delete(housePicture.getPath());
            if (reponse.isOK()) {
                housePictureRepository.delete(id);
                return ServiceResult.success();
            }
            return new ServiceResult(false, reponse.error);
        } catch (QiniuException e) {
            e.printStackTrace();
            return new ServiceResult(false, e.getMessage());
        }
    }

    @Override
    @Transactional
    public ServiceResult updateCover(Long coverId, Long targetId) {
        HousePicture housePicture = housePictureRepository.findOne(coverId);
        if (housePicture == null) {
            return ServiceResult.notFound();
        }
        houseRepository.updateCover(targetId, housePicture.getPath());
        return ServiceResult.success();
    }

    @Override
    public ServiceResult addTag(Long houseId, String tag) {
        House house = houseRepository.findOne(houseId);
        if (house == null) {
            return ServiceResult.notFound();
        }
        HouseTag houseTag = houseTagRepository.findByNameAndHouseId(tag, houseId);
        if (houseTag != null) {
            return new ServiceResult(false, "标签已存在");
        }
        houseTagRepository.save(new HouseTag(houseId, tag));
        return ServiceResult.success();
    }

    @Override
    public ServiceResult removeTag(Long houseId, String tag) {
        House house = houseRepository.findOne(houseId);
        if (house == null) {
            return ServiceResult.notFound();
        }
        HouseTag houseTag = houseTagRepository.findByNameAndHouseId(tag, houseId);
        if (houseTag == null) {
            return new ServiceResult(false, "标签不存在");
        }
        houseTagRepository.delete(houseTag.getId());
        return ServiceResult.success();
    }

    @Override
    @Transactional
    public ServiceResult updateStatus(Long id, int status) {
        House house = this.houseRepository.findOne(id);
        if (house == null) {
            return ServiceResult.notFound();
        }
        if (house.getStatus() == HouseStatus.DELETED.getValue()) {
            return new ServiceResult(false, "该房屋资源已被删除，不能进行操作");
        }
        houseRepository.updateStatus(id, status);
        /**
         * 除过通过审核，其他删除索引
         */
        if (status == HouseStatus.PASSES.getValue()) {
            searchService.index(id);
        } else {
            searchService.remove(id);
        }
        return ServiceResult.success();
    }

    private List<HouseDTO> wrapperHouseResult(List<Long> houseIds) {
        List<HouseDTO> result = new ArrayList<>();
        Map<Long, HouseDTO> idToHouseMap = new HashMap<>();
        Iterable<House> houseIdIterable = houseRepository.findAll(houseIds);
        houseIdIterable.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(house.getCover());
            houseDTO.setCover(this.cdnPrefix + house.getCover());
            idToHouseMap.put(house.getId(), houseDTO);
        });

        wrapperHouseList(houseIds, idToHouseMap);
        //矫正顺序
        for (Long houseId : houseIds) {
            result.add(idToHouseMap.get(houseId));
        }
        return result;
    }

    @Override
    public ServiceMultiResult<HouseDTO> query(RentSearch rentSearch) {
        if (rentSearch.getKeywords() != null && !rentSearch.getKeywords().equals("*")) {
            ServiceMultiResult<Long> query = searchService.query(rentSearch);
            if (query.getTotal() == 0) {
                return new ServiceMultiResult<HouseDTO>(0, new ArrayList<>());
            }
            return new ServiceMultiResult<HouseDTO>(query.getTotal(), wrapperHouseResult(query
                    .getResult()));
        }
        return simpleQuery(rentSearch);
    }

    @Override
    public ServiceMultiResult<HouseDTO> wholeMapQuery(MapSearch mapSearch) {
        ServiceMultiResult<Long> longServiceMultiResult = searchService.mapQuery(mapSearch
                .getCityEnName(), mapSearch.getOrderBy(), mapSearch
                .getOrderDirection(), mapSearch.getStart(), mapSearch.getSize());
        if (longServiceMultiResult.getTotal() == 0) {
            return new ServiceMultiResult<>(0, new ArrayList<>());
        }
        List<HouseDTO> houseDTOS = wrapperHouseResult(longServiceMultiResult.getResult());
        return new ServiceMultiResult<>(longServiceMultiResult.getTotal(), houseDTOS);
    }

    @Override
    public ServiceMultiResult<HouseDTO> boundMapQuery(MapSearch mapSearch) {
        ServiceMultiResult<Long> longServiceMultiResult = searchService.mapQuery(mapSearch);
        if (longServiceMultiResult.getTotal() == 0) {
            return new ServiceMultiResult<>(0, new ArrayList<>());
        }
        List<HouseDTO> houseDTOS = wrapperHouseResult(longServiceMultiResult.getResult());

        return new ServiceMultiResult<>(longServiceMultiResult.getTotal(), houseDTOS);
    }

    @Override
    @Transactional
    public ServiceResult addSubscribeOrder(Long houseId) {
        Long userId = LoginUserUtil.getLoginUserId();
        HouseSubscribe houseSubscribe = subscribeRespository.findByHouseIdAndUserId(houseId,
                userId);
        if (houseSubscribe != null) {
            return new ServiceResult(false, "已加入预约");
        }
        House house = houseRepository.findOne(houseId);
        if (house == null) {
            return new ServiceResult(false, "查无此房");
        }
        houseSubscribe = new HouseSubscribe();
        Date now = new Date();
        houseSubscribe.setCreateTime(now);
        houseSubscribe.setLastUpdateTime(now);
        houseSubscribe.setOrderTime(now);
        houseSubscribe.setHouseId(houseId);
        houseSubscribe.setUserId(userId);
        houseSubscribe.setStatus(SubscribeEnum.IN_ORDER_LIST.getValue());
        houseSubscribe.setAdminId(house.getAdminId());
        subscribeRespository.save(houseSubscribe);
        return ServiceResult.success();
    }

    @Override
    public ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> querySubscribeList(SubscribeEnum
                                                                                                status, int start, int size) {
        Long userId = LoginUserUtil.getLoginUserId();
        Pageable pageable = new PageRequest(start / size, size, new Sort(Sort.Direction.DESC,
                "createTime"));
        Page<HouseSubscribe> houseSubscribes = subscribeRespository.findAllByUserIdAndStatus
                (userId, status.getValue(), pageable);
        return wrapper(houseSubscribes);
    }

    @Override
    @Transactional
    public ServiceResult subscribe(Long houseId, Date orderTime, String telephone, String desc) {
        Long userId = LoginUserUtil.getLoginUserId();
        HouseSubscribe houseSubscribe = subscribeRespository.findByHouseIdAndUserId(houseId,
                userId);
        if(houseSubscribe == null){
            return new ServiceResult(false,"无预约记录");
        }
        if(houseSubscribe.getStatus() != SubscribeEnum.IN_ORDER_LIST.getValue()){
            return new ServiceResult(false,"无法预约");
        }
        houseSubscribe.setStatus(SubscribeEnum.IN_ORDER_TIME.getValue());
        houseSubscribe.setOrderTime(orderTime);
        houseSubscribe.setLastUpdateTime(new Date());
        houseSubscribe.setDesc(desc);
        houseSubscribe.setTelephone(telephone);
        subscribeRespository.save(houseSubscribe);
        return ServiceResult.success();
    }

    @Override
    @Transactional
    public ServiceResult cancelSubscribe(Long houseId) {
        Long userId = LoginUserUtil.getLoginUserId();
        HouseSubscribe houseSubscribe = subscribeRespository.findByHouseIdAndUserId(houseId,
                userId);
        if(houseSubscribe == null){
            return new ServiceResult(false,"无预约记录");
        }
        if(houseSubscribe.getStatus() != SubscribeEnum.IN_ORDER_TIME.getValue()){
            return new ServiceResult(false,"无预约记录");
        }
        subscribeRespository.delete(houseSubscribe.getId());
        return ServiceResult.success();
    }

    @Override
    public ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> findSubscribeList(int start, int
            size) {
        Long userId = LoginUserUtil.getLoginUserId();
        Pageable pageable = new PageRequest(start/size,size,new Sort(Sort.Direction.DESC,
                "orderTime"));
        Page<HouseSubscribe> page = subscribeRespository
                .findAllByAdminIdAndStatus(userId, SubscribeEnum.IN_ORDER_TIME
                .getValue(), pageable);
        return wrapper(page);
    }

    @Override
    @Transactional
    public ServiceResult finishSubscribe(Long houseId) {
        Long adminId = LoginUserUtil.getLoginUserId();
        HouseSubscribe houseIdAndAdminId = subscribeRespository.findByHouseIdAndAdminId
                (houseId, adminId);
        if(houseIdAndAdminId == null){
            return new ServiceResult(false,"无预约记录");
        }
        subscribeRespository.updateStatus(houseIdAndAdminId.getId(),SubscribeEnum.FINISH.getValue());
        houseRepository.updateWatchTimes(houseId);
        return ServiceResult.success();
    }

    private ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> wrapper(Page<HouseSubscribe>
                                                                                  page) {
        List<Pair<HouseDTO, HouseSubscribeDTO>> result = new ArrayList<>();
        if (page.getSize() < 1) {
            return new ServiceMultiResult<>(page.getTotalElements(), result);
        }
        List<HouseSubscribeDTO> subscribeDTOS = new ArrayList<>();
        List<Long> houseIds = new ArrayList<>();
        Map<Long, HouseDTO> idTOHouseMap = new HashMap<>();
        page.forEach(houseSubscribe -> {
            subscribeDTOS.add(modelMapper.map(houseSubscribe, HouseSubscribeDTO.class));
            houseIds.add(houseSubscribe.getHouseId());
        });
        Iterable<House> houses = houseRepository.findAll(houseIds);
        houses.forEach(house -> {
            idTOHouseMap.put(house.getId(), modelMapper.map(house, HouseDTO.class));
        });
        for (HouseSubscribeDTO dto : subscribeDTOS) {
            Pair<HouseDTO, HouseSubscribeDTO> pair = Pair.of(idTOHouseMap.get(dto.getHouseId()),
                    dto);
            result.add(pair);
        }
        return new ServiceMultiResult<>(page.getTotalElements(), result);

    }


    private ServiceMultiResult<HouseDTO> simpleQuery(RentSearch rentSearch) {
        Sort sort = HouseSort.generateSort(rentSearch.getOrderBy(), rentSearch.getOrderDirection());
        int page = rentSearch.getStart() / rentSearch.getSize();

        Pageable pageable = new PageRequest(page, rentSearch.getSize(), sort);

        Specification specification = (root, criteriaQuery, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.equal(root.get("status"), HouseStatus.PASSES
                    .getValue());
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get
                    ("cityEnName"), rentSearch.getCityEnName()));
            if (HouseSort.DISTANCE_TO_SUBWAY_KEY.equals(rentSearch.getOrderBy())) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.gt(root.get(HouseSort
                        .DISTANCE_TO_SUBWAY_KEY), -1));
            }
            return predicate;
        };
        Page<House> houseRepositoryAll = houseRepository.findAll(specification, pageable);
        List<HouseDTO> houseDTOS = new ArrayList<>();
        List<Long> houseIds = new ArrayList<>();
        Map<Long, HouseDTO> idToHouseMap = Maps.newHashMap();
        houseRepositoryAll.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
//            houseDTO.setCover(house.getCover());
            houseDTO.setCover(this.cdnPrefix + house.getCover());
            houseDTOS.add(houseDTO);

            houseIds.add(house.getId());
            idToHouseMap.put(house.getId(), houseDTO);
        });
        wrapperHouseList(houseIds, idToHouseMap);
        return new ServiceMultiResult<>(houseRepositoryAll.getTotalElements(), houseDTOS);
    }

    /**
     * 渲染详细信息及标签
     *
     * @param houseIds
     * @param idToHouseMap
     */
    private void wrapperHouseList(List<Long> houseIds, Map<Long, HouseDTO> idToHouseMap) {
        List<HouseDetail> details = houseDetailRepository.findAllByHouseIdIn(houseIds);
        details.forEach(houseDetail -> {
            HouseDTO houseDTO = idToHouseMap.get(houseDetail.getHouseId());
            HouseDetailDTO houseDetailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);
            houseDTO.setHouseDetail(houseDetailDTO);
        });

        List<HouseTag> housets = houseTagRepository.findAllByHouseIdIn(houseIds);
        housets.forEach(houseTag -> {
            HouseDTO houseDTO = idToHouseMap.get(houseTag.getHouseId());
            houseDTO.getTags().add(houseTag.getName());
        });
    }

    /**
     * 图片对象列表信息填充
     *
     * @param form
     * @param houseId
     * @return
     */
    private List<HousePicture> generatePictures(HouseForm form, Long houseId) {
        List<HousePicture> pictures = new ArrayList<>();
        if (form.getPhotos() == null || form.getPhotos().isEmpty()) {
            return pictures;
        }
        for (PhotoForm photoForm : form.getPhotos()) {
            HousePicture picture = new HousePicture();
            picture.setHouseId(houseId);
            picture.setCdnPrefix(cdnPrefix);
            picture.setPath(photoForm.getPath());
            picture.setWidth(photoForm.getWidth());
            picture.setHeight(photoForm.getHeight());
            pictures.add(picture);
        }
        return pictures;
    }

    /**
     * 房源详细信息对象填充
     *
     * @param houseDetail
     * @param houseForm
     * @return
     */
    private ServiceResult<HouseDTO> wrapperDetailInfo(HouseDetail houseDetail, HouseForm
            houseForm) {
        Subway subway = subwayRepository.findOne(houseForm.getSubwayLineId());
        if (subway == null) {
            return new ServiceResult<HouseDTO>(false, "Not vaild subway line!");
        }

        SubwayStation subwayStation = subwayStationRepository.findOne(houseForm
                .getSubwayStationId());
        if (subwayStation == null || subway.getId() != subwayStation.getSubwayId()) {
            return new ServiceResult<HouseDTO>(false, "Not vaild subway Station!");
        }

        houseDetail.setSubwayLineId(subway.getId());
        houseDetail.setSubwayLineName(subway.getName());

        houseDetail.setSubwayStationId(subwayStation.getId());
        houseDetail.setSubwayStationName(subwayStation.getName());

        houseDetail.setDescription(houseForm.getDescription());
        houseDetail.setDetailAddress(houseForm.getDetailAddress());
        houseDetail.setLayoutDesc(houseForm.getLayoutDesc());
        houseDetail.setRentWay(houseForm.getRentWay());
        houseDetail.setRoundService(houseForm.getRoundService());
        houseDetail.setTraffic(houseForm.getTraffic());
        return null;
    }
}
