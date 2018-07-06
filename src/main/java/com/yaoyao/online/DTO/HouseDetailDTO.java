package com.yaoyao.online.DTO;

import lombok.Data;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 13:49
 * @Description:
 */
@Data
public class HouseDetailDTO {
    private String description;

    private String layoutDesc;

    private String traffic;

    private String roundService;

    private int rentWay;

    private Long adminId;

    private String address;

    private Long subwayLineId;

    private Long subwayStationId;

    private String subwayLineName;

    private String subwayStationName;
}
