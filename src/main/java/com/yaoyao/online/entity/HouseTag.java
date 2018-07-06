package com.yaoyao.online.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 12:00
 * @Description:
 */
@Entity
@Data
@Table(name = "house_tag")
public class HouseTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "house_id")
    private Long houseId;

    private String name;

    public HouseTag() {
    }

    public HouseTag(Long houseId, String name) {
        this.houseId = houseId;
        this.name = name;
    }
}
