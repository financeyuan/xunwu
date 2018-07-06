package com.yaoyao.online.base;

import com.google.common.collect.ImmutableMap;
import lombok.Data;

import java.util.Map;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/8 12:37
 * @Description:带区间的常用数值定义
 */
@Data
public class RentValueBlock {

    /**
     * 价格定义区间
     */
    public static final Map<String, RentValueBlock> PRICE_BLOCK;

    /**
     * 面的定义区间
     */
    public static final Map<String, RentValueBlock> AREA_BLOCK;

    public static final RentValueBlock ALL = new RentValueBlock("*", -1, -1);

    static {
        PRICE_BLOCK = ImmutableMap.<String, RentValueBlock>builder()
                .put("*-500", new RentValueBlock("*-500", -1, 500))
                .put("500-1000", new RentValueBlock("500-1000", 500, 1000))
                .put("1000-1500", new RentValueBlock("1000-1500", 1000, 1500))
                .put("1500-3000", new RentValueBlock("1500-3000", 1500, 3000))
                .put("3000-*", new RentValueBlock("3000-*", 3000, -1))
                .build();
        AREA_BLOCK = ImmutableMap.<String, RentValueBlock>builder()
                .put("*-30", new RentValueBlock("*-30", -1, 30))
                .put("30-60", new RentValueBlock("30-60", 30, 60))
                .put("60-90", new RentValueBlock("60-90", 60, 90))
                .put("90-120", new RentValueBlock("90-120", 90, 120))
                .put("120-*", new RentValueBlock("120-*", 120, -1))
                .build();
    }

    private String key;

    private int min;

    private int max;

    public RentValueBlock(String key, int min, int max) {
        this.key = key;
        this.min = min;
        this.max = max;
    }

    public static RentValueBlock matchPrice(String key){
        RentValueBlock block = PRICE_BLOCK.get(key);
        if(block == null){
            return ALL;
        }
        return block;
    }

    public static RentValueBlock matchArea(String key){
        RentValueBlock block = AREA_BLOCK.get(key);
        if(block == null){
            return ALL;
        }
        return block;
    }

}
