package com.yaoyao.online.base;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

/**  
* Title: AppErrorController 
* @author yuanpb  
* @date 2018年6月1日  
* Error全局处理
*/  
@Controller
public class AppErrorController implements ErrorController{
	
	private static final String ERROR_PATH = "/error"; 

	private ErrorAttributes errorAttributes;
	
	@Override
	public String getErrorPath() {
		return ERROR_PATH;
	}
	
	@Autowired
	public AppErrorController(ErrorAttributes errorAttributes) {
		this.errorAttributes = errorAttributes;
	}
	
	/**  
	 * @param request
	 * @param response
	 * @return  
	 * web页面错误处理
	 */  
	@RequestMapping(value = ERROR_PATH,produces = "text/html")
	public String errorPageHandler(HttpServletRequest request,HttpServletResponse response) {
		int status = response.getStatus();
		switch (status) {
		case 403:
			return "403";
		case 404:
			return "403";
		case 500:
			return "500";
		}
		return "index";
	}
	
	/**  
	 * @param request
	 * @return  
	 * api异常处理
	 */  
	@RequestMapping(value = ERROR_PATH)
	@ResponseBody
	public ApiResponse errorApiHandler(HttpServletRequest request) {
		RequestAttributes requestAttributes = new ServletRequestAttributes(request);
		Map<String,Object> attribute = this.errorAttributes.getErrorAttributes(requestAttributes, false);
		int status = getStatus(request);
		return ApiResponse.ofMessage(status, String.valueOf(attribute.getOrDefault("message", "error")));
	}
	
	private int getStatus(HttpServletRequest request) {
		Integer status = (Integer) request.getAttribute("javax.servlet.error.status_code");
		if(status != null) {
			return status;
		}
		return 500;
	}
}
