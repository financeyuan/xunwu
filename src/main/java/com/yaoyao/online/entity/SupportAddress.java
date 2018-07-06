package com.yaoyao.online.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 10:42
 * @Description:
 */
@Entity
@Table(name = "support_address")
@Data
public class SupportAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "belong_to")
    private String belongTo;

    @Column(name = "en_name")
    private String enName;

    @Column(name = "cn_name")
    private String cnName;

    private String level;

    @Column(name = "baidu_map_lng")
    private double baiduMapLongitude;

    @Column(name = "baidu_map_lat")
    private double baiduMapLatitude;

    /**
     * 行政级别枚举
     */
    public enum Level{
        CITY("city"),
        REGION("region");
        private String value;
        Level(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Level of(String value){
            for (Level level : Level.values()) {
                if(level.getValue().equals(value)){
                    return level;
                }
            }
            throw new IllegalArgumentException();
        }
    }

}
