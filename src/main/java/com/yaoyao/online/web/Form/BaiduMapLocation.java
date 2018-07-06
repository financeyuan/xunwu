package com.yaoyao.online.web.Form;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/27 14:42
 * @Description:百度位置信息
 */
@Data
public class BaiduMapLocation {

    /**
     * 经度
     */
    @JsonProperty("lon")
    private double longitude;

    /**
     * 纬度
     */
    @JsonProperty("lat")
    private double latitude;

}
