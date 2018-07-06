package com.yaoyao.online.DTO;

import lombok.Data;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/8 16:02
 * @Description:
 */
@Data
public class UserDTO {
    private Long id;
    private String name;
    private String avatar;
    private String phoneNumber;
    private String lastLoginTime;

}
