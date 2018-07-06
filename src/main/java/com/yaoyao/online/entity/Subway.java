package com.yaoyao.online.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 12:02
 * @Description:
 */
@Entity
@Data
@Table(name = "subway")
public class Subway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String  name;

    @Column(name = "city_en_name")
    private String  cityEnName;
}
