package com.yaoyao.online.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.yaoyao.online.entity.Role;

public interface RoleRepository extends CrudRepository<Role, Long>{
	List<Role> findByUserId(Long UserId);
}
