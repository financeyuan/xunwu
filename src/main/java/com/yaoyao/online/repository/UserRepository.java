package com.yaoyao.online.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.yaoyao.online.entity.User;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository<User, Long>{
	
	User findByName(String UserName);

    User findByPhoneNumber(String telephone);

    @Modifying
    @Query("update User as user set user.name = :name where id = :id")
    void updateUsername(@Param(value = "id") Long id,@Param(value = "name")String name);

    @Modifying
    @Query("update User as user set user.password = :password where id = :id")
    void updatePassword(@Param(value = "id") Long id,@Param(value = "password")String password);

    @Modifying
    @Query("update User as user set user.email = :email where id = :id")
    void updateEmail(@Param(value = "id") Long id,@Param(value = "email")String email);

}
