package com.yaoyao.online.web.Form;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/7 09:54
 * @Description:datatables表单
 */
@Data
public class DataTableSearch {

    /**
     * datatables固定回显字段
     */
    private int draw;

    /**
     * datatables分页字段
     */
    private int start;

    private int length;

    private Integer status;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createTimeMin;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date getCreateTimeMax;

    private String city;

    private String title;

    private String direction;

    private String orderBy;
}
