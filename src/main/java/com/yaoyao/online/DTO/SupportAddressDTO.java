package com.yaoyao.online.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 11:01
 * @Description:
 */
@Data
public class SupportAddressDTO {

    private Long Id;

    @JsonProperty(value = "belong_to")
    private String belongTo;

    @JsonProperty(value = "en_name")
    private String enName;

    @JsonProperty(value = "cn_name")
    private String cnName;

    private String Level;

    private double baiduMapLongitude;

    private double baiduMapLatitude;
}
