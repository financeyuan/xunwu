package com.yaoyao.online.base;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/29 09:16
 * @Description:预约状态码
 */
public enum SubscribeEnum {
    NO_SUBSCRIBE(0),//未预约
    IN_ORDER_LIST(1),//加入预约清单
    IN_ORDER_TIME(2),//已经预约看房
    FINISH(3);//已完成预约

    private int value;

    SubscribeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SubscribeEnum of(int value){
        for (SubscribeEnum subscribeEnum : SubscribeEnum.values()) {
            if(subscribeEnum.getValue() == value){
                return subscribeEnum;
            }
        }
        return SubscribeEnum.NO_SUBSCRIBE;
    }
}
