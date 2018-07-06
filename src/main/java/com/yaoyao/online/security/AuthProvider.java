package com.yaoyao.online.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.yaoyao.online.entity.User;
import com.yaoyao.online.service.IUserService;

/**
 * 自定义认证实现 Title: AuthProvider
 * 
 * @author yuanpb
 * @date 2018年6月1日
 */
public class AuthProvider implements AuthenticationProvider {

	@Autowired
	private IUserService userService;

	private final Md5PasswordEncoder md5PasswordEncoder = new Md5PasswordEncoder();

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		String inputPassword = (String) authentication.getCredentials();
		User user = userService.findByUserName(username);
		if (user == null) {
			throw new AuthenticationCredentialsNotFoundException("authError");
		}

		if (this.md5PasswordEncoder.isPasswordValid(user.getPassword(), inputPassword, user.getId())) {
			return new UsernamePasswordAuthenticationToken(user, inputPassword,user.getAuthorities());
		}
		throw new BadCredentialsException("authError");
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return true;
	}

}
