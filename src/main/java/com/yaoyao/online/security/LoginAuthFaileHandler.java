package com.yaoyao.online.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

/**
 * 登陆验证失败
* Title: LoginAuthFaileHandler 
* @author yuanpb  
* @date 2018年6月1日
 */
public class LoginAuthFaileHandler extends SimpleUrlAuthenticationFailureHandler{
	
	private final LoginUrlEntryPoint urlEntryPoint;
	
	public LoginAuthFaileHandler(LoginUrlEntryPoint urlEntryPoint) {
		this.urlEntryPoint = urlEntryPoint;
	}
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		String targeurl = this.urlEntryPoint.determineUrlToUseForThisRequest(request, response, exception);
		targeurl += "?"+exception.getMessage();
		super.setDefaultFailureUrl(targeurl);
		super.onAuthenticationFailure(request, response, exception);
	}
	
}
