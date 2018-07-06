package com.yaoyao.online.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/15 16:38
 * @Description:消息引擎模板
 */
@Data
public class HouseIndexMessage implements Serializable {

    public static final String INDEX = "index";

    public static final String REMOVE = "remove";

    public static final int MAX_RETRY = 3;

    private static final long serialVersionUID = -1217516454062216787L;

    private Long houseId;

    private String operation;

    private int retry = 0;

    public HouseIndexMessage(Long houseId, String operation, int retry) {
        this.houseId = houseId;
        this.operation = operation;
        this.retry = retry;
    }
}
