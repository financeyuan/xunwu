package com.yaoyao.online.web.controller;

import com.yaoyao.online.base.ApiResponse;
import com.yaoyao.online.base.LoginUserUtil;
import com.yaoyao.online.base.ServiceResult;
import com.yaoyao.online.service.ISmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

	@Autowired
	private ISmsService smsService;
	
    @GetMapping(value = {"/", "/index"})
    public String index(Model model) {
        return "index";
    }
	
	@GetMapping("403")
	public String accessEoor() {
		return "403";
	}
	
	@GetMapping("404")
	public String NotFoundError() {
		return "404";
	}
	
	@GetMapping("500")
	public String internalError() {
		return "500";
	}
	
	@GetMapping("logout/page")
	public String logoutPage() {
		return "logout";
	}

	@GetMapping("/sms/code")
	@ResponseBody
	public ApiResponse getSms(@RequestParam("telephone") String telephone){
		if(!LoginUserUtil.checkTelephone(telephone)){
			return  ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(),"请输入正确的手机号");
		}

		ServiceResult<String> result = smsService.sendSms(telephone);
		if(result.isSuccess()){
			return ApiResponse.ofSuccess("");
		}
		return  ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(),result.getMessage());
	}
	
}
