package com.yaoyao.online.config;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;

import com.qiniu.storage.BucketManager;
import com.qiniu.util.Auth;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.MultipartProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;

import com.qiniu.common.Zone;
import com.qiniu.storage.UploadManager;

/**
 * 文件上传组件
* Title: WebUploadConfig 
* @author yuanpb  
* @date 2018年6月1日
 */
@Configuration
@ConditionalOnClass({Servlet.class,StandardServletMultipartResolver.class,MultipartConfigElement.class})
@ConditionalOnProperty(prefix = "spring.http.multipart",name = "enabled",matchIfMissing = true)
@EnableConfigurationProperties(MultipartProperties.class)
public class WebUploadConfig {
	
	private final MultipartProperties multipartProperties;
	
	public WebUploadConfig(MultipartProperties multipartProperties) {
		this.multipartProperties = multipartProperties;
	}
	
	/**
	 * 上传配置
	 */
	@Bean
	@ConditionalOnMissingBean
	public MultipartConfigElement multipartConfigElement() {
		return this.multipartProperties.createMultipartConfig();
	}
	
	/**
	 * 注册解析器
	 */
	@Bean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
	@ConditionalOnMissingBean(MultipartResolver.class)
	public StandardServletMultipartResolver multipartResolver() {
		StandardServletMultipartResolver multipartResolver = new StandardServletMultipartResolver();
		multipartResolver.setResolveLazily(this.multipartProperties.isResolveLazily());
		return multipartResolver;
	}
	
	/**
	 * 华东机房
	 */
	@Bean
	public com.qiniu.storage.Configuration qinniuConfig(){
		return new com.qiniu.storage.Configuration(Zone.zone0());
	}
	
	/**
	 * 构建一个七牛上传实例
	 */
	@Bean
	public UploadManager uploadManager() {
		return new UploadManager(qinniuConfig());
	}

	@Value("${qunniu.accessKey}")
	private String accessKey;

	@Value("${qunniu.secretKey}")
	private String secretKey;

	/**
	 * 认证信息实例
	 * @return
	 */
	@Bean
	public Auth auth(){
		return  Auth.create(accessKey,secretKey);
	}

	/**
	 * 构建七牛空间管理
	 */
	@Bean
	public BucketManager bucketManager(){
		return new BucketManager(auth(),qinniuConfig());
	}
}
