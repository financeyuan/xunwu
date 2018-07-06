package com.yaoyao.online.web.controller.admin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.model.DefaultPutRet;
import com.yaoyao.online.DTO.*;
import com.yaoyao.online.base.*;
import com.yaoyao.online.entity.SupportAddress;
import com.yaoyao.online.service.IAddressService;
import com.yaoyao.online.service.IHouseService;
import com.yaoyao.online.service.IQiNiuService;
import com.yaoyao.online.service.IUserService;
import com.yaoyao.online.web.Form.DataTableSearch;
import com.yaoyao.online.web.Form.HouseForm;
import org.elasticsearch.search.aggregations.pipeline.AbstractPipelineAggregationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@Controller
public class AdminController {

    @Autowired
    private IAddressService addressService;

    @Autowired
    private IHouseService houseService;

    @Autowired
    private IQiNiuService qiNiuService;

    @Autowired
    private IUserService userService;


    @GetMapping("/admin/center")
    public String adminCenter() {
        return "/admin/center";
    }

    @GetMapping("/admin/welcome")
    public String adminWelcome() {
        return "/admin/welcome";
    }

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "/admin/login";
    }

    @GetMapping("admin/add/house")
    public String andHousePage() {
        return "admin/house-add";
    }

    @GetMapping("/admin/house/edit")
    public String editHousePage(@RequestParam(value = "id") Long id, Model model) {
        if (id == null) {
            return "404";
        }
        ServiceResult serviceResult = houseService.findCompleteOne(id);
        if (!serviceResult.isSuccess()) {
            return "404";
        }
        HouseDTO result = (HouseDTO) serviceResult.getResult();
        model.addAttribute("house", result);
        Map<SupportAddress.Level, SupportAddressDTO> cityAndRegion = addressService
                .findCityAndRegion(result.getCityEnName(), result.getRegionEnName());
        model.addAttribute("city", cityAndRegion.get(SupportAddress.Level.CITY));
        model.addAttribute("region", cityAndRegion.get(SupportAddress.Level.REGION));
        HouseDetailDTO houseDetailDTO = result.getHouseDetail();
        ServiceResult<SubwayDTO> subway = addressService.findSubway(houseDetailDTO
                .getSubwayLineId());
        if (subway.isSuccess()) {
            model.addAttribute("subway", subway.getResult());
        }
        ServiceResult<SubwayStationDTO> station = addressService.findSubwayStation
                (houseDetailDTO.getSubwayStationId());
        if (station.isSuccess()) {
            model.addAttribute("station", station.getResult());
        }
        return "admin/house-edit";
    }

    /**
     * 编辑接口
     *
     * @param houseForm
     * @return
     */
    @PostMapping("/admin/house/edit")
    @ResponseBody
    public ApiResponse saveHouse(@Valid @ModelAttribute("form-house-edit") HouseForm houseForm,
                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), bindingResult.getAllErrors()
                    .get(0).getDefaultMessage(), null);
        }
        Map<SupportAddress.Level, SupportAddressDTO> cityAndRegion = addressService
                .findCityAndRegion(houseForm.getCityEnName(), houseForm.getRegionEnName());
        if (cityAndRegion.keySet().size() != 2) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VAILD_PARAMS);
        }
        ServiceResult result = houseService.Update(houseForm);
        if (result.isSuccess()) {
            return ApiResponse.ofSuccess(null);
        }
        ApiResponse apiResponse = ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        apiResponse.setMessage(result.getMessage());
        return apiResponse;
    }


    @PostMapping("/admin/houses")
    @ResponseBody
    public ApiDataTablesResponse houses(@ModelAttribute DataTableSearch searchBody) {
        ServiceMultiResult<HouseDTO> result = houseService.adminQuery(searchBody);
        ApiDataTablesResponse response = new ApiDataTablesResponse(ApiResponse.Status.SUCCESS);
        response.setData(result.getResult());
        response.setRecordsFiltered(result.getTotal());
        response.setRecordsTotal(result.getTotal());
        response.setDraw(searchBody.getDraw());
        return response;
    }

    /**
     * 房源列表页
     *
     * @return
     */
    @GetMapping("/admin/house/list")
    public String houseListPage() {
        return "admin/house-list";
    }

    @PostMapping(value = "/admin/upload/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ApiResponse uploadPhoto(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VAILD_PARAMS);
        }

        String fileName = file.getOriginalFilename();

        try {
            InputStream inputStream = file.getInputStream();
            Response response = qiNiuService.uploadFile(inputStream);
            if (response.isOK()) {
                //QiNiuPutRet ret = gson.fromJson(response.bodyString(), QiNiuPutRet.class);
                QiNiuPutRet ret = JSON.parseObject(response.bodyString(), QiNiuPutRet.class);
                return ApiResponse.ofSuccess(ret);
            } else {
                return ApiResponse.ofMessage(response.statusCode, response.getInfo());
            }

        } catch (QiniuException e) {
            Response response = e.response;
            try {
                return ApiResponse.ofMessage(response.statusCode, response.bodyString());
            } catch (QiniuException e1) {
                e1.printStackTrace();
                return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException e) {
            return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/admin/add/house")
    @ResponseBody
    public ApiResponse addHouse(@Valid @ModelAttribute("form-house-add") HouseForm houseForm,
                                BindingResult bindingResult) {
        /**
         * 参数有误
         */
        if (bindingResult.hasErrors()) {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), bindingResult.getAllErrors()
                    .get(0)
                    .getDefaultMessage(), null);
        }

        /**
         * 参数或者封面不存在
         */
        if (houseForm.getPhotos() == null || houseForm.getCover() == null) {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "必须上传图片");
        }

        Map<SupportAddress.Level, SupportAddressDTO> addressMap = addressService
                .findCityAndRegion(houseForm.getCityEnName(), houseForm.getRegionEnName());
        if (addressMap.keySet().size() != 2) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VAILD_PARAMS);
        }

        ServiceResult<HouseDTO> result = houseService.save(houseForm);
        if (result.isSuccess()) {
            return ApiResponse.ofSuccess(result.getResult());
        }
        return ApiResponse.ofSuccess(ApiResponse.Status.NOT_VAILD_PARAMS);
    }

    /**
     * 移除图片接口
     *
     * @param id
     * @return
     */
    @DeleteMapping("admin/house/photo")
    @ResponseBody
    public ApiResponse removeHousePhoto(@RequestParam(value = "id") Long id) {
        ServiceResult result = this.houseService.removePhoto(id);

        if (result.isSuccess()) {
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        } else {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
        }
    }

    /**
     * 修改封面接口
     *
     * @param coverId
     * @param targetId
     * @return
     */
    @PostMapping("admin/house/cover")
    @ResponseBody
    public ApiResponse updateCover(@RequestParam(value = "cover_id") Long coverId,
                                   @RequestParam(value = "target_id") Long targetId) {
        ServiceResult result = this.houseService.updateCover(coverId, targetId);

        if (result.isSuccess()) {
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        } else {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
        }
    }

    /**
     * 增加标签接口
     *
     * @param houseId
     * @param tag
     * @return
     */
    @PostMapping("admin/house/tag")
    @ResponseBody
    public ApiResponse addHouseTag(@RequestParam(value = "house_id") Long houseId,
                                   @RequestParam(value = "tag") String tag) {
        if (houseId < 1 || Strings.isNullOrEmpty(tag)) {
            return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }

        ServiceResult result = this.houseService.addTag(houseId, tag);
        if (result.isSuccess()) {
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        } else {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
        }
    }

    /**
     * 移除标签接口
     *
     * @param houseId
     * @param tag
     * @return
     */
    @DeleteMapping("admin/house/tag")
    @ResponseBody
    public ApiResponse removeHouseTag(@RequestParam(value = "house_id") Long houseId,
                                      @RequestParam(value = "tag") String tag) {
        if (houseId < 1 || Strings.isNullOrEmpty(tag)) {
            return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }

        ServiceResult result = this.houseService.removeTag(houseId, tag);
        if (result.isSuccess()) {
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        } else {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
        }
    }


    /**
     * 审核接口
     *
     * @param id
     * @param operation
     * @return
     */
    @PutMapping("/admin/house/operate/{id}/{operation}")
    @ResponseBody
    public ApiResponse operateHouse(@PathVariable(value = "id") Long id, @PathVariable(value =
            "operation") int operation) {
        ServiceResult result;
        switch (operation) {
            case HouseOperation.PASS:
                result = this.houseService.updateStatus(id, HouseStatus.PASSES.getValue
                        ());
                break;
            case HouseOperation.PULL_OUT:
                result = this.houseService.updateStatus(id, HouseStatus.NOT_AUDITED.getValue
                        ());
                break;
            case HouseOperation.DELETE:
                result = this.houseService.updateStatus(id, HouseStatus.DELETED.getValue
                        ());
                break;
            case HouseOperation.RENT:
                result = this.houseService.updateStatus(id, HouseStatus.RENTED.getValue
                        ());
                break;
            default:
                return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }
        if(result.isSuccess()){
            return ApiResponse.ofSuccess(null);
        }
        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(),result.getMessage());
    }

    @GetMapping("/admin/house/subscribe")
    public String houseSubscribe(){
        return "admin/subscribe";
    }

    @GetMapping("/admin/house/subscribe/list")
    @ResponseBody
    public ApiResponse subscirbeList(@RequestParam(value = "draw") int draw,@RequestParam
            (value = "length") int size,@RequestParam(value = "start")int start){
        ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> subscribeList = houseService
                .findSubscribeList(start, size);
        ApiDataTablesResponse response = new ApiDataTablesResponse(ApiResponse.Status.SUCCESS);
        response.setData(subscribeList.getResult());
        response.setDraw(draw);
        response.setRecordsTotal(subscribeList.getTotal());
        response.setRecordsFiltered(subscribeList.getTotal());
        return response;
    }

    @GetMapping("/admin/user/{userId}")
    @ResponseBody
    public ApiResponse getUserInfo(@PathVariable(value = "userId")Long userId){
        if(userId == null || userId < 1){
            return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }
        ServiceResult<UserDTO> result = userService.findById(userId);
        if(!result.isSuccess()){
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(result.getResult());
    }

    @PostMapping("/admin/finish/subscribe")
    @ResponseBody
    public ApiResponse finishSubscribe(@RequestParam(value = "house_id") Long houseId){
        if(houseId < 1){
            return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }
        ServiceResult result = houseService.finishSubscribe(houseId);
        if(result.isSuccess()){
            return ApiResponse.ofSuccess("");
        }
        return ApiResponse.ofMessage(ApiResponse.Status.BAD_REQUEST.getCode(),result.getMessage());
    }

}
