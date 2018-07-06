package com.yaoyao.online.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaoyao.online.DTO.SubwayDTO;
import com.yaoyao.online.DTO.SubwayStationDTO;
import com.yaoyao.online.DTO.SupportAddressDTO;
import com.yaoyao.online.base.ServiceMultiResult;
import com.yaoyao.online.base.ServiceResult;
import com.yaoyao.online.entity.Subway;
import com.yaoyao.online.entity.SubwayStation;
import com.yaoyao.online.entity.SupportAddress;
import com.yaoyao.online.repository.SubwayRepository;
import com.yaoyao.online.repository.SubwayStationRepository;
import com.yaoyao.online.repository.SupportAddressRepository;
import com.yaoyao.online.service.IAddressService;
import com.yaoyao.online.service.ISearchService;
import com.yaoyao.online.web.Form.BaiduMapLocation;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 11:05
 * @Description:
 */
@Service
public class AddressServiceImpl implements IAddressService {

    @Autowired
    private SupportAddressRepository supportAddressRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private SubwayRepository subwayRepository;

    @Autowired
    private SubwayStationRepository subwayStationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.baidu.map.key}")
    private String BAIDU_MAP_KEY;

    @Value("${spring.baidu.map.geoconv.api}")
    private String BAIDU_MAP_GEOCONV_API;

    @Value("${spring.baidu.map.lbs.create}")
    private String BAIDU_LBS_CREATE_API;

    @Value("${spring.baidu.map.lbs.update}")
    private String BAIDU_LBS_UPDATA_API;

    @Value("${spring.baidu.map.lbs.query}")
    private String BAIDU_LBS_QUERY_API;

    @Value("${spring.baidu.map.lbs.delete}")
    private String BAIDU_LBS_DELETE_API;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(IAddressService.class);

    /**
     * @return
     */
    @Override
    public ServiceMultiResult<SupportAddressDTO> findAllCities() {
        List<SupportAddress> address = supportAddressRepository.findAllByLevel(SupportAddress.Level.CITY
                .getValue());
        List<SupportAddressDTO> addressDTOS = new ArrayList<>();
        for (SupportAddress supportAddress : address) {
            SupportAddressDTO target = modelMapper.map(supportAddress, SupportAddressDTO.class);
            addressDTOS.add(target);
        }
        return new ServiceMultiResult(addressDTOS.size(), addressDTOS);
    }

    /**
     * @param cityEnName
     * @param regionEnName
     * @return
     */
    @Override
    public Map<SupportAddress.Level, SupportAddressDTO> findCityAndRegion(String cityEnName, String regionEnName) {
        Map<SupportAddress.Level, SupportAddressDTO> result = new HashMap<>();
        SupportAddress city = supportAddressRepository.findByEnNameAndLevel(cityEnName, SupportAddress.Level.CITY
                .getValue());
        SupportAddress region = supportAddressRepository.findByEnNameAndBelongTo(regionEnName, city.getEnName());
        result.put(SupportAddress.Level.CITY, modelMapper.map(city, SupportAddressDTO.class));
        result.put(SupportAddress.Level.REGION, modelMapper.map(region, SupportAddressDTO.class));
        return result;
    }

    @Override
    public ServiceMultiResult findAllRegionsByCityName(String cityName) {
        if (cityName == null) {
            return new ServiceMultiResult<>(0, null);
        }

        List<SupportAddressDTO> result = new ArrayList<>();

        List<SupportAddress> regions = supportAddressRepository.findAllByLevelAndBelongTo(SupportAddress.Level.REGION
                .getValue(), cityName);
        for (SupportAddress region : regions) {
            result.add(modelMapper.map(region, SupportAddressDTO.class));
        }
        return new ServiceMultiResult<>(regions.size(), result);
    }

    @Override
    public List<SubwayDTO> findAllSubwayByCity(String cityEnName) {
        List<SubwayDTO> result = new ArrayList<>();
        List<Subway> subways = subwayRepository.findAllByCityEnName(cityEnName);
        if (subways.isEmpty()) {
            return result;
        }

        subways.forEach(subway -> result.add(modelMapper.map(subway, SubwayDTO.class)));
        return result;
    }

    @Override
    public List<SubwayStationDTO> findAllStationBySubway(Long subwayId) {
        List<SubwayStationDTO> result = new ArrayList<>();
        List<SubwayStation> stations = subwayStationRepository.findAllBySubwayId(subwayId);
        if (stations.isEmpty()) {
            return result;
        }

        stations.forEach(station -> result.add(modelMapper.map(station, SubwayStationDTO.class)));
        return result;
    }

    @Override
    public ServiceResult<SubwayDTO> findSubway(Long subwayId) {
        if (subwayId == null) {
            return ServiceResult.notFound();
        }
        Subway subway = subwayRepository.findOne(subwayId);
        if (subway == null) {
            return ServiceResult.notFound();
        }
        return ServiceResult.of(modelMapper.map(subway, SubwayDTO.class));
    }

    @Override
    public ServiceResult<SubwayStationDTO> findSubwayStation(Long stationId) {
        if (stationId == null) {
            return ServiceResult.notFound();
        }
        SubwayStation station = subwayStationRepository.findOne(stationId);
        if (station == null) {
            return ServiceResult.notFound();
        }
        return ServiceResult.of(modelMapper.map(station, SubwayStationDTO.class));
    }

    @Override
    public ServiceResult<SupportAddressDTO> findCity(String cityEnName) {
        if (cityEnName == null) {
            return ServiceResult.notFound();
        }

        SupportAddress supportAddress = supportAddressRepository.findByEnNameAndLevel(cityEnName, SupportAddress.Level.CITY.getValue());
        if (supportAddress == null) {
            return ServiceResult.notFound();
        }

        SupportAddressDTO addressDTO = modelMapper.map(supportAddress, SupportAddressDTO.class);
        return ServiceResult.of(addressDTO);
    }

    @Override
    public ServiceResult<BaiduMapLocation> getBaiduMapLocation(String city, String address) {

        String encodeAddress;
        String encodeCity;
        try {
            encodeCity = URLEncoder.encode(city, "UTF-8");
            encodeAddress = URLEncoder.encode(address, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("Error to encode house address", e);
            return new ServiceResult<>(false, "Error to encode house address");
        }

        HttpClient httpClient = HttpClients.createDefault();
        StringBuilder sb = new StringBuilder(BAIDU_MAP_GEOCONV_API);
        sb.append("address=").append(encodeAddress).append("&").append("city=").append
                (encodeCity).append("&").append("output=json&").append("ak=").append(BAIDU_MAP_KEY);
        HttpGet httpGet = new HttpGet(sb.toString());
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                return new ServiceResult<>(false, "can not get baidu map localtion");
            }
            String result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            JsonNode jsonNode = objectMapper.readTree(result);
            int status = jsonNode.get("status").asInt();
            if (status != 0) {
                return new ServiceResult<>(false, "Error get baidu map localtion for status" + status);
            }
            BaiduMapLocation baiduMapLocation = new BaiduMapLocation();
            JsonNode jsonLocaltion = jsonNode.get("result").get("location");
            baiduMapLocation.setLatitude(jsonLocaltion.get("lat").asDouble());
            baiduMapLocation.setLongitude(jsonLocaltion.get("lng").asDouble());
            return ServiceResult.of(baiduMapLocation);
        } catch (IOException e) {
            logger.error("Error to fetch baidumap api", e);
            return new ServiceResult<>(false, "Error to fetch baidumap api");
        }
    }

    @Override
    public ServiceResult lbsUpload(BaiduMapLocation location, String title, String address, long
            houseId, int price, int area) {
        HttpClient httpClient = HttpClients.createDefault();
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("latitude", String.valueOf(location.getLatitude())));
        nvps.add(new BasicNameValuePair("longitude", String.valueOf(location.getLongitude())));
        nvps.add(new BasicNameValuePair("coord_type", "3"));//百度坐标系
        nvps.add(new BasicNameValuePair("geotable_id", "191178"));
        nvps.add(new BasicNameValuePair("ak", BAIDU_MAP_KEY));
        nvps.add(new BasicNameValuePair("houseid", String.valueOf(houseId)));
        nvps.add(new BasicNameValuePair("price", String.valueOf(price)));
        nvps.add(new BasicNameValuePair("area", String.valueOf(area)));
        nvps.add(new BasicNameValuePair("title", String.valueOf(title)));
        nvps.add(new BasicNameValuePair("address", String.valueOf(address)));
        HttpPost post;
        if (isLbsDataExits(houseId)) {
            post = new HttpPost(BAIDU_LBS_UPDATA_API);
        } else {
            post = new HttpPost(BAIDU_LBS_CREATE_API);
        }
        try {
            post.setEntity(new UrlEncodedFormEntity(nvps,"UTF-8"));
            HttpResponse execute = httpClient.execute(post);
            String result = EntityUtils.toString(execute.getEntity(), "UTF-8");
            if(execute.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
                logger.error("Can not response " + result);
                return  new ServiceResult(false,"Can not response " + result);
            }
            JsonNode jsonNode = objectMapper.readTree(result);
            int status = jsonNode.get("status").asInt();
            if(status != 0){
                String message = jsonNode.get("message").asText();
                logger.error("Error to upload lbs data for status:{},and message:{}",status,message);
                return  new ServiceResult(false,"Error to upload lbs data");
            }
            return ServiceResult.success();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  new ServiceResult(false);
    }

    @Override
    public ServiceResult removeLbs(long houseId) {
        if(!isLbsDataExits(houseId)){
            return new ServiceResult(true);
        }
        HttpClient httpClient = HttpClients.createDefault();
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("geotable_id", "191178"));
        nvps.add(new BasicNameValuePair("houseid", String.valueOf(houseId)));
        nvps.add(new BasicNameValuePair("ak", BAIDU_MAP_KEY));
        HttpPost delete = new HttpPost(BAIDU_LBS_DELETE_API);
        try {
            delete.setEntity(new UrlEncodedFormEntity(nvps,"UTF-8"));
            HttpResponse execute = httpClient.execute(delete);
            String s = EntityUtils.toString(execute.getEntity(), "UTF-8");
            if(execute.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
                logger.error("Error to delete lbs data for  response:"+s);
                return new ServiceResult(false);
            }
            JsonNode jsonNode = objectMapper.readTree(s);
            int status = jsonNode.get("status").asInt();
            if(status != 0){
                String message = jsonNode.get("message").asText();
                logger.error("Error to delete lbs data for  message "+message);
                return new ServiceResult(false,"Error to delete lbs data for  message "+message);
            }
            return ServiceResult.success();
        } catch (IOException e) {
            logger.error("Error to delete lbs data for  message ",e);
            return new ServiceResult(false);
        }
    }

    private boolean isLbsDataExits(long houseId) {
        HttpClient httpClient = HttpClients.createDefault();
        StringBuilder sb = new StringBuilder(BAIDU_LBS_QUERY_API);
        sb.append("geotable_id=191178").append("&").append("ak").append(BAIDU_MAP_KEY).append
                ("&").append("houseid=").append(houseId).append(",").append(houseId);
        HttpGet httpGet = new HttpGet(sb.toString());
        try {
            HttpResponse execute = httpClient.execute(httpGet);
            String result = EntityUtils.toString(execute.getEntity(), "UTF-8");
            if (execute.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.error("can not get http " + result);
                return false;
            }
            JsonNode jsonNode = objectMapper.readTree(result);
            int status = jsonNode.get("status").asInt();
            if (status != 0) {
                logger.error("Error to get lbs data for status" + status);
                return false;
            }
            long size = jsonNode.get("size").asLong();
            if (size > 0) {
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
