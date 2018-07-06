package com.yaoyao.online.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 11:56
 * @Description:
 */
@Entity
@Table(name = "house_subscribe")
@Data
public class HouseSubscribe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "house_id")
    private Long houseId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "`desc`")
    private String desc;

    private int status;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "last_update_time")
    private Date lastUpdateTime;

    @Column(name = "order_time")
    private Date orderTime;

    private String telephone;

    @Column(name = "admin_id")
    private Long adminId;
}
