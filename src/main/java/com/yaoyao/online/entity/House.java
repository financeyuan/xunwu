package com.yaoyao.online.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 11:33
 * @Description:
 */
@Table(name = "house")
@Entity
@Data
public class House {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private int price;

    private int area;

    private int room;

    private int floor;

    @Column(name = "total_floor")
    private int totalFloor;

    @Column(name = "watch_times")
    private int watchTimes;

    @Column(name = "build_year")
    private int buildYear;

    private int status;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "last_update_time")
    private Date lastUpdateTime;

    @Column(name = "city_en_name")
    private String cityEnName;

    @Column(name = "region_en_name")
    private String regionEnName;

    private String cover;

    private int direction;

    @Column(name = "distance_to_subway")
    private int distanceToSubway;

    private int parlour;

    private String district;

    @Column(name = "admin_id")
    private Long adminId;

    private int bathroom;

    private String street;


}
