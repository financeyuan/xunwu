package com.yaoyao.online.web.Form;

import lombok.Data;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/8 10:52
 * @Description:请求参数结构体
 */
@Data
public class RentSearch {

    private String cityEnName;
    private String regionEnName;
    private String priceBlock;
    private String areaBlock;
    private int room;
    private int direction;
    private String keywords;
    private int rentWay = -1;
    private String orderBy = "lastUpdateTime";
    private String orderDirection = "desc";

    private int start = 0;

    private int size = 5;

    public int getStart() {
        return start > 0 ? start : 0;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getSize() {
        if (this.size < 1) {
            return 5;
        } else if (this.size > 100) {
            return 100;
        } else {
            return this.size;
        }
    }

    public void setSize(int size) {
        this.size = size;
    }


    public int getRentWay() {
        if (rentWay > -2 && rentWay < 2) {
            return rentWay;
        } else {
            return -1;
        }
    }

    public void setRentWay(int rentWay) {
        this.rentWay = rentWay;
    }

    @Override
    public String toString() {
        return "RentSearch {" +
                "cityEnName='" + cityEnName + '\'' +
                ", regionEnName='" + regionEnName + '\'' +
                ", priceBlock='" + priceBlock + '\'' +
                ", areaBlock='" + areaBlock + '\'' +
                ", room=" + room +
                ", direction=" + direction +
                ", keywords='" + keywords + '\'' +
                ", rentWay=" + rentWay +
                ", orderBy='" + orderBy + '\'' +
                ", orderDirection='" + orderDirection + '\'' +
                ", start=" + start +
                ", size=" + size +
                '}';
    }

}
