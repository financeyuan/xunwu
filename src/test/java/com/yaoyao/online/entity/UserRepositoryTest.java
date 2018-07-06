package com.yaoyao.online.entity;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;

import com.yaoyao.online.HouseonlineApplicationTests;
import com.yaoyao.online.repository.UserRepository;

public class UserRepositoryTest extends HouseonlineApplicationTests{
	
	@Resource
	private UserRepository userRepository;
	
	@Test
	public void findOneTest() {
		User user = userRepository.findOne(1L);
		Assert.assertEquals("waliwali", user.getName());
	}
}
