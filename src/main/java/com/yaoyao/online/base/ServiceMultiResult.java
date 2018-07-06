package com.yaoyao.online.base;

import lombok.Data;

import java.util.List;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 11:16
 * @Description:通用结果返回结构
 */
@Data
public class ServiceMultiResult<T> {

    private long total;

    private List<T> result;

    public ServiceMultiResult(long total, List<T> result) {
        this.total = total;
        this.result = result;
    }

    public int getResultSize(){
        if(this.result == null){
            return 0;
        }
        return this.result.size();
    }
}
