package com.yaoyao.online.DTO;

import lombok.Data;

import javax.persistence.Column;
import java.util.Date;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/29 08:57
 * @Description:
 */
@Data
public class HouseSubscribeDTO {

    private Long id;

    private Long houseId;

    private Long userId;

    private String desc;

    private int status;

    private Date createTime;

    private Date lastUpdateTime;

    private Date orderTime;

    private String telephone;

    private Long adminId;
}
