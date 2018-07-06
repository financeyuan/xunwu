package com.yaoyao.online.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 11:47
 * @Description:
 */
@Data
@Entity
@Table(name = "house_detail")
public class HouseDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Column(name = "layout_desc")
    private String layoutDesc;

    private String traffic;

    @Column(name = "round_service")
    private String roundService;

    @Column(name = "rent_way")
    private int rentWay;

    @Column(name = "address")
    private String detailAddress;

    @Column(name = "subway_line_id")
    private Long subwayLineId;

    @Column(name = "subway_line_name")
    private String subwayLineName;

    @Column(name = "subway_station_id")
    private Long subwayStationId;

    @Column(name = "subway_station_name")
    private String subwayStationName;

    @Column(name = "house_id")
    private Long houseId;
}
