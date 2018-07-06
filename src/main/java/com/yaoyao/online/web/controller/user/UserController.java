package com.yaoyao.online.web.controller.user;

import com.yaoyao.online.DTO.HouseDTO;
import com.yaoyao.online.DTO.HouseSubscribeDTO;
import com.yaoyao.online.base.*;
import com.yaoyao.online.service.IHouseService;
import com.yaoyao.online.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IHouseService houseService;

    @GetMapping(value = "/user/login")
    public String loginPage() {
        return "user/login";
    }

    @GetMapping(value = "/user/center")
    public String centerPage() {
        return "user/center";
    }

    @PostMapping(value = "/api/user/info")
    @ResponseBody
    public ApiResponse updateUserInfo(@RequestParam(value = "profile") String profile,
                                      @RequestParam(value = "value") String value) {
        if (value.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }
        if ("email".equals(profile) && !LoginUserUtil.checkEmail(profile)) {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "不支持的邮箱格式");
        }
        ServiceResult result = userService.modifyUserProfile(profile, value);
        if (result.isSuccess()) {
            return ApiResponse.ofSuccess("");
        }
        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
    }

    @PostMapping("/api/user/house/subscribe")
    @ResponseBody
    public ApiResponse subscribeHouse(@RequestParam(value = "house_id")Long houseId){
        ServiceResult result = houseService.addSubscribeOrder(houseId);
        if(result.isSuccess()){
            return ApiResponse.ofSuccess("");
        }
        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(),result.getMessage());
    }

    @GetMapping("/api/user/house/subscribe/list")
    @ResponseBody
    public ApiResponse subscribeList(@RequestParam(value = "size",defaultValue = "5") int size,
                                     @RequestParam(value = "start",defaultValue = "0") int start,
                                     @RequestParam(value = "status") int status){
        ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> pairServiceMultiResult =
                houseService.querySubscribeList(SubscribeEnum.of(status), start, size);
        if(pairServiceMultiResult.getResultSize() == 0){
            return ApiResponse.ofSuccess(pairServiceMultiResult.getResult());
        }
        ApiResponse apiResponse = ApiResponse.ofSuccess(pairServiceMultiResult.getResult());
        apiResponse.setMore(pairServiceMultiResult.getTotal() > (size+start));
        return  apiResponse;
    }

    @PostMapping("/api/user/house/subscribe/date")
    @ResponseBody
    public ApiResponse subscribeDate(@RequestParam(value = "houseId") Long houseId,
                                     @RequestParam(value = "orderTime") @DateTimeFormat(pattern="yyyy-MM-dd") Date orderTime,
                                     @RequestParam(value="desc",required = false) String desc,
                                     @RequestParam(value = "telephone")String telephone){
        if(orderTime == null){
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(),"请选择预约时间");
        }
        if(!LoginUserUtil.checkTelephone(telephone)){
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(),"请输入正确得手机号");
        }
        ServiceResult subscribe = houseService.subscribe(houseId, orderTime, telephone, desc);
        if(subscribe.isSuccess()){
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        }
        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(),subscribe.getMessage());

    }

    @DeleteMapping("/api/user/house/subscribe")
    @ResponseBody
    public ApiResponse cancelSubscribe(@RequestParam(value = "houseId") Long houseId){
        ServiceResult result = houseService.cancelSubscribe(houseId);
        if(result.isSuccess()){
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        }
        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(),result.getMessage());
    }


}
