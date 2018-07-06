package com.yaoyao.online.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 11:53
 * @Description:
 */
@Table(name = "house_picture")
@Entity
@Data
public class HousePicture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "house_id")
    private Long houseId;

    @Column(name = "cdn_prefix")
    private String cdnPrefix;

    private int width;

    private int height;

    private String location;

    private String path;
}
