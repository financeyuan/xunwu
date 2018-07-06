package com.yaoyao.online.service;

import com.yaoyao.online.web.Form.BaiduMapLocation;
import com.yaoyao.online.web.Form.HouseSuggest;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/13 10:07
 * @Description:索引结构模板
 */
@Data
public class HouseIndexTemplate {

    private Long houseId;

    private String title;

    private int price;

    private int area;

    private Date createTime;

    private Date lastUpdateTime;

    private String cityEnName;

    private String regionEnName;

    private int direction;

    private int distanceToSubway;

    private String subwayLineName;

    private String subwayStationName;

    private String street;

    private String district;

    private String description;

    private String layoutDesc;

    private String traffic;

    private String roundService;

    private int rentWay;

    private List<String> tags;

    private List<HouseSuggest> suggests;

    private BaiduMapLocation baiduMapLocation;

}
