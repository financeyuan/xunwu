package com.yaoyao.online.web.controller.house;

import com.yaoyao.online.DTO.*;
import com.yaoyao.online.base.ApiResponse;
import com.yaoyao.online.base.RentValueBlock;
import com.yaoyao.online.base.ServiceMultiResult;
import com.yaoyao.online.base.ServiceResult;
import com.yaoyao.online.entity.SupportAddress;
import com.yaoyao.online.service.IAddressService;
import com.yaoyao.online.service.IHouseService;
import com.yaoyao.online.service.ISearchService;
import com.yaoyao.online.service.IUserService;
import com.yaoyao.online.web.Form.MapSearch;
import com.yaoyao.online.web.Form.RentSearch;
import org.elasticsearch.cluster.routing.allocation.RerouteExplanation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 10:41
 * @Description:house控制器
 */
@Controller
public class HouseController {

    @Autowired
    private IAddressService addressService;

    @Autowired
    private IHouseService houseService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ISearchService searchService;


    /**
     * 自动补全
     *
     * @param
     * @return
     */
    @GetMapping("rent/house/autocomplete")
    @ResponseBody
    public ApiResponse autocomplete(@RequestParam(value = "prefix") String prefix) {
        if (prefix.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }
        ServiceResult<List<String>> result = this.searchService.suggest(prefix);
//        List<String> result  = new ArrayList<>();
//        result.add("测试");
        return ApiResponse.ofSuccess(result.getResult());
    }

    /**
     * 获取城市支持列表
     *
     * @return
     */
    @GetMapping(value = "/address/support/cities")
    @ResponseBody
    public ApiResponse getSupportsCities() {
        ServiceMultiResult<SupportAddressDTO> result = addressService.findAllCities();
        if (result.getResultSize() == 0) {
            return ApiResponse.ofSuccess(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(result.getResult());
    }

    @GetMapping("address/support/regions")
    @ResponseBody
    public ApiResponse getSupportRegions(@RequestParam(name = "city_name") String cityEnName) {
        ServiceMultiResult<SupportAddressDTO> addressResult = addressService
                .findAllRegionsByCityName(cityEnName);
        if (addressResult.getResult() == null || addressResult.getTotal() < 1) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(addressResult.getResult());
    }

    /**
     * 获取具体城市所支持的地铁线路
     *
     * @param cityEnName
     * @return
     */
    @GetMapping("address/support/subway/line")
    @ResponseBody
    public ApiResponse getSupportSubwayLine(@RequestParam(name = "city_name") String cityEnName) {
        List<SubwayDTO> subways = addressService.findAllSubwayByCity(cityEnName);
        if (subways.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }

        return ApiResponse.ofSuccess(subways);
    }

    /**
     * 获取对应地铁线路所支持的地铁站点
     *
     * @param subwayId
     * @return
     */
    @GetMapping("address/support/subway/station")
    @ResponseBody
    public ApiResponse getSupportSubwayStation(@RequestParam(name = "subway_id") Long subwayId) {
        List<SubwayStationDTO> stationDTOS = addressService.findAllStationBySubway(subwayId);
        if (stationDTOS.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }

        return ApiResponse.ofSuccess(stationDTOS);
    }

    @GetMapping("/rent/house")
    public String rentHousePage(@ModelAttribute RentSearch rentSearch, Model model, HttpSession
            session, RedirectAttributes redirectAttributes) {
        if (rentSearch.getCityEnName() == null) {
            String cityEnNameInSession = (String) session.getAttribute("cityEnName");
            if (cityEnNameInSession == null) {
                redirectAttributes.addAttribute("msg", "must_chose_city");
                return "redirect:/index";
            } else {
                rentSearch.setCityEnName(cityEnNameInSession);
            }
        } else {
            session.setAttribute("cityEnName", rentSearch.getCityEnName());
        }
        ServiceResult<SupportAddressDTO> city = addressService.findCity(rentSearch.getCityEnName());
        if (!city.isSuccess()) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }
        model.addAttribute("currentCity", city.getResult());

        ServiceMultiResult<SupportAddressDTO> addressResult = addressService
                .findAllRegionsByCityName
                        (rentSearch.getCityEnName());
        if (addressResult.getResult() == null || addressResult.getTotal() < 1) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }

        ServiceMultiResult<HouseDTO> serviceMultiResult = houseService.query(rentSearch);

        model.addAttribute("total", serviceMultiResult.getTotal());
//        model.addAttribute("total", 0);
//        model.addAttribute("houses", new ArrayList<>());
        model.addAttribute("houses", serviceMultiResult.getResult());
        if (rentSearch.getRegionEnName() == null) {
            rentSearch.setRegionEnName("*");
        }
        model.addAttribute("searchBody", rentSearch);
        model.addAttribute("regions", addressResult.getResult());
        model.addAttribute("priceBlocks", RentValueBlock.PRICE_BLOCK);
        model.addAttribute("areaBlocks", RentValueBlock.AREA_BLOCK);

        model.addAttribute("currentPriceBlock", RentValueBlock.matchPrice(rentSearch
                .getPriceBlock()));
        model.addAttribute("currentAreaBlock", RentValueBlock.matchPrice(rentSearch.getAreaBlock
                ()));
        return "rent-list";

    }

    @GetMapping("/rent/house/show/{id}")
    public String show(@PathVariable(value = "id") Long houseId, Model model) {
        if (houseId < 0) {
            return "404";
        }
        ServiceResult<HouseDTO> serviceResult = houseService.findCompleteOne(houseId);
        if (!serviceResult.isSuccess()) {
            return "404";
        }
        HouseDTO houseDto = serviceResult.getResult();
        Map<SupportAddress.Level, SupportAddressDTO> cityAndRegion = addressService
                .findCityAndRegion(houseDto.getCityEnName(), houseDto.getRegionEnName());
        SupportAddressDTO city = cityAndRegion.get(SupportAddress.Level.CITY);
        SupportAddressDTO region = cityAndRegion.get(SupportAddress.Level.REGION);
        model.addAttribute("city", city);
        model.addAttribute("region", region);
        ServiceResult<UserDTO> serviceUserDTORestlt = userService.findById(houseDto.getAdminId());
        model.addAttribute("agent", serviceUserDTORestlt.getResult());
        model.addAttribute("house", houseDto);
        ServiceResult<Long> result = searchService.aggregateDistrictHouse(city
                .getEnName(), region.getEnName(), houseDto.getDistrict());
        model.addAttribute("houseCountInDistrict", result.getResult());
        return "house-detail";
    }

    @GetMapping("rent/house/map")
    public String rentMapPage(@RequestParam(value = "cityEnName") String cityEnName,
                              Model model, HttpSession session, RedirectAttributes
                                          redirectAttributes) {
        ServiceResult<SupportAddressDTO> city = addressService.findCity(cityEnName);
        if (!city.isSuccess()) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }
        session.setAttribute("cityEnName", cityEnName);
        model.addAttribute("city", city.getResult());
        ServiceMultiResult<SupportAddressDTO> allRegionsByCityName = addressService
                .findAllRegionsByCityName(cityEnName);

        ServiceMultiResult<HouseBucketDTO> houseBucketDTOServiceMultiResult = searchService
                .mapAggregate(cityEnName);

        model.addAttribute("aggData", houseBucketDTOServiceMultiResult.getResult());
        model.addAttribute("totle", houseBucketDTOServiceMultiResult.getTotal());
        model.addAttribute("regions", allRegionsByCityName.getResult());
        return "rent-map";
    }

    @GetMapping("/rent/house/map/houses")
    @ResponseBody
    public ApiResponse rentMapHouse(@ModelAttribute MapSearch mapSearch) {

        if (mapSearch.getCityEnName() == null) {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "必须选择城市");
        }
        ServiceMultiResult<HouseDTO> serviceMultiResult;
        if(mapSearch.getLevel() < 13){
            serviceMultiResult = houseService.wholeMapQuery(mapSearch);
        }else{
            /**
             * 小地图查询必传边界参数
             */
            serviceMultiResult = houseService.boundMapQuery(mapSearch);
        }
        ApiResponse response = ApiResponse.ofSuccess(serviceMultiResult.getResult());
        response.setMore(serviceMultiResult.getTotal() > (mapSearch.getStart() + mapSearch.getSize()));
        return response;
    }


}
