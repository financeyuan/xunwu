package com.yaoyao.online.DTO;

import lombok.Data;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/27 12:45
 * @Description:
 */
@Data
public class HouseBucketDTO {

    /**
     * 聚合bucker的key
     */
    private String key;

    /**
     * 聚合结果值
     */
    private long count;

    public HouseBucketDTO(String key, long count) {
        this.key = key;
        this.count = count;
    }
}
