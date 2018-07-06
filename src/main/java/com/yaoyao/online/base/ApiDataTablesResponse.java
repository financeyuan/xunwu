package com.yaoyao.online.base;

import lombok.Data;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/7 09:50
 * @Description:datatables响应结构
 */
@Data
public class ApiDataTablesResponse extends ApiResponse {

    private int draw;

    private long recordsTotal;

    private long recordsFiltered;

    public ApiDataTablesResponse(Integer code, String message, Object data) {
        super(code, message, data);
    }

    public ApiDataTablesResponse(ApiResponse.Status status) {
        this(status.getCode(), status.getStandardMessage(), null);
    }

}
