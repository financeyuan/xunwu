package com.yaoyao.online.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 13:50
 * @Description:
 */
@Data
public class HousePictureDTO {

    private Long id;

    @JsonProperty(value = "house_id")
    private Long houseId;

    private String path;

    @JsonProperty(value = "cdn_prefix")
    private String cdnPrefix;

    private int width;

    private int height;
}
