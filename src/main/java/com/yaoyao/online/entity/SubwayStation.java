package com.yaoyao.online.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 12:04
 * @Description:
 */
@Entity
@Data
@Table(name = "subway_station")
public class SubwayStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subway_id")
    private Long    subwayId;

    private String  name;
}
