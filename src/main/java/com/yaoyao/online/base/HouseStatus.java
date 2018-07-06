package com.yaoyao.online.base;

import lombok.Data;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/7 10:46
 * @Description:房源状态
 */
public enum HouseStatus {
    NOT_AUDITED(0), //未审核
    PASSES(1),//审核通过
    RENTED(2),//已出租
    DELETED(3);//逻辑删除

    private int value;

    HouseStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
