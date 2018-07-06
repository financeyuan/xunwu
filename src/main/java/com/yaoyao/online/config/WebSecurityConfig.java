package com.yaoyao.online.config;

import com.yaoyao.online.security.AuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.yaoyao.online.security.AuthProvider;
import com.yaoyao.online.security.LoginAuthFaileHandler;
import com.yaoyao.online.security.LoginUrlEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@EnableWebSecurity
@EnableGlobalMethodSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{
	
	/**
	 * http權限控制
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.addFilterBefore(authFilter(),UsernamePasswordAuthenticationFilter.class);
		http.authorizeRequests()
		.antMatchers("/admin/login").permitAll()
		.antMatchers("/static/**").permitAll()
		.antMatchers("/user/login").permitAll()
		.antMatchers("/admin/**").hasRole("ADMIN")
		.antMatchers("/user/**").hasAnyRole("ADMIN","USER")
		.antMatchers("/api/user/**").hasAnyRole("ADMIN","USER")
		 .and()
         .formLogin()
         .loginProcessingUrl("/login") //配置角色登录处理入口
         .failureHandler(authFaileHandler())
         .and()
         .logout()
         .logoutUrl("/logout")
         .logoutSuccessUrl("/logout/page")
         .deleteCookies("JSESSIONID")
         .invalidateHttpSession(true)
         .and()
         .exceptionHandling()
         .authenticationEntryPoint(urlEntryPoint())
         .accessDeniedPage("/403");
		 http.csrf().disable();
	        http.headers().frameOptions().sameOrigin();
	}
	
	/**
	 * 自定义策略
	 */
	public void configGlobal(AuthenticationManagerBuilder auth) {
		auth.authenticationProvider(authProvider()).eraseCredentials(true);
	}
	
	@Bean
	public AuthProvider authProvider() {
		return new AuthProvider();
	}
	
    @Bean
    public LoginUrlEntryPoint urlEntryPoint() {
        return new LoginUrlEntryPoint("/user/login");
    }
    
    @Bean
    public LoginAuthFaileHandler authFaileHandler() {
    	return new LoginAuthFaileHandler(urlEntryPoint());
    }

    @Bean
    public AuthFilter authFilter(){
		AuthFilter authFilter = new AuthFilter();
		authFilter.setAuthenticationManager(authenticationManager());
		authFilter.setAuthenticationFailureHandler(authFaileHandler());
		return authFilter;
	}

	@Bean
	public AuthenticationManager authenticationManager(){
		AuthenticationManager authenticationManager = null;
		try {
			authenticationManager = super.authenticationManager();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return authenticationManager;
	}
}
