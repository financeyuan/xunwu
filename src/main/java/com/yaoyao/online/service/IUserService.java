package com.yaoyao.online.service;

import com.yaoyao.online.DTO.UserDTO;
import com.yaoyao.online.base.ServiceResult;
import com.yaoyao.online.entity.User;
/**
 * 用户基本接口
* Title: IUserService 
* @author yuanpb  
* @date 2018年6月1日
 */
public interface IUserService {
	
	User findByUserName(String User);

    ServiceResult<UserDTO> findById(Long userId);

    /**
     * 根据手机号码查询用户
     * @param telephone
     * @return
     */
    User findByTelephone(String telephone);

    /**
     * 通过手机号码注册用户
     * @param telephone
     * @return
     */
    User addUserByPhone(String telephone);

    /**
     * 修改指定属性值
     * @param profile
     * @param value
     * @return
     */
    ServiceResult modifyUserProfile(String profile,String value);
}
