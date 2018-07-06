package com.yaoyao.online.security;

import com.google.common.base.Strings;
import com.yaoyao.online.base.LoginUserUtil;
import com.yaoyao.online.entity.User;
import com.yaoyao.online.service.ISmsService;
import com.yaoyao.online.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/28 11:31
 * @Description:
 */
public class AuthFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired
    private IUserService userService;

    @Autowired
    private ISmsService smsService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse
            response) throws AuthenticationException {
        String name  = obtainUsername(request);
        if(!Strings.isNullOrEmpty(name)){
            request.setAttribute("username",name);
            return super.attemptAuthentication(request, response);
        }
        String telephone = request.getParameter("telephone");
        if (Strings.isNullOrEmpty(telephone) || !LoginUserUtil.checkTelephone(telephone)) {
            throw new BadCredentialsException("Wrong telephone number");
        }
        User user = userService.findByTelephone(telephone);
        String inputCode = request.getParameter("smsCode");
        String sessionCode = smsService.getSmsCode(telephone);
        if(Objects.equals(inputCode,sessionCode)){
            /**
             * 如果用户手机第一次登陆，并未进行注册则自动注册该用户
             */
            if(user == null){
                user = userService.addUserByPhone(telephone);
            }
            return new UsernamePasswordAuthenticationToken(user,null,user.getAuthortyList());
        }else {
            throw new BadCredentialsException("smsCodeError");
        }
    }
}
