package com.yaoyao.online.service;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;

import java.io.File;
import java.io.InputStream;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/5 16:42
 * @Description:七牛服务接口
 */
public interface IQiNiuService {

    Response uploadFile (File file) throws QiniuException;

    Response uploadFile(InputStream inputStream) throws QiniuException;

    Response delete(String key) throws QiniuException;
}
