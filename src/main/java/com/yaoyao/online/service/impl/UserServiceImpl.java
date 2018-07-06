package com.yaoyao.online.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.yaoyao.online.DTO.UserDTO;
import com.yaoyao.online.base.LoginUserUtil;
import com.yaoyao.online.base.RentValueBlock;
import com.yaoyao.online.base.ServiceResult;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.yaoyao.online.entity.Role;
import com.yaoyao.online.entity.User;
import com.yaoyao.online.repository.RoleRepository;
import com.yaoyao.online.repository.UserRepository;
import com.yaoyao.online.service.IUserService;

import javax.transaction.Transactional;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModelMapper modelMapper;

    private final Md5PasswordEncoder md5PasswordEncoder = new Md5PasswordEncoder();

    @Override
    public User findByUserName(String UserName) {
        User user = userRepository.findByName(UserName);
        if (user == null) {
            return null;
        }
        List<Role> roles = roleRepository.findByUserId(user.getId());
        if (roles.isEmpty() || roles == null) {
            throw new DisabledException("权限非法");
        }
        List<GrantedAuthority> authoritys = new ArrayList<>();
        roles.forEach(role -> authoritys.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));
        user.setAuthortyList(authoritys);
        return user;

    }

    @Override
    public ServiceResult<UserDTO> findById(Long userId) {
        User user = userRepository.findOne(userId);
        if (user == null) {
            return ServiceResult.notFound();
        }
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return ServiceResult.of(userDTO);
    }

    @Override
    public User findByTelephone(String telephone) {
        User user = userRepository.findByPhoneNumber(telephone);
        if (user == null) {
            return null;
        }
        List<Role> roles = roleRepository.findByUserId(user.getId());
        if (roles.isEmpty() || roles == null) {
            throw new DisabledException("权限非法");
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        });
        user.setAuthortyList(authorities);
        return user;
    }

    @Override
    @Transactional
    public User addUserByPhone(String telephone) {
        User user = new User();
        user.setPhoneNumber(telephone);
        user.setName(telephone.substring(0, 3) + "****" + telephone.substring(telephone.length()
						- 3,
                telephone.length()));
        Date now = new Date();
        user.setCreateTime(now);
        user.setLastLoginTime(now);
        user.setLastUpdateTime(now);
        user = userRepository.save(user);

        Role role = new Role();
        role.setUserId(user.getId());
        role.setName("USER");
        roleRepository.save(role);
        user.setAuthortyList(Lists.newArrayList(new SimpleGrantedAuthority("ROLE_USER")));
        return user;
    }

    @Override
    @Transactional
    public ServiceResult modifyUserProfile(String profile, String value) {
        Long userid = LoginUserUtil.getLoginUserId();
        if (Strings.isNullOrEmpty(profile)) {
            return new ServiceResult(false, "属性不可为空");
        }
        switch (profile) {
            case "name":
                userRepository.updateUsername(userid, value);
                break;
            case "email":
                userRepository.updateEmail(userid, value);
                break;
            case "password":
                userRepository.updatePassword(userid, this.md5PasswordEncoder
                        .encodePassword(value, userid));
                break;
            default:
                return new ServiceResult(false, "不支持的属性");

        }
        return ServiceResult.success();
    }

}
