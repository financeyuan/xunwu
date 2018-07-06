package com.yaoyao.online.service;

import com.yaoyao.online.base.ServiceResult;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/28 11:26
 * @Description:短信验证
 */
public interface ISmsService {
    /**
     * 发送验证码,并进行缓存10分钟，及请求间隔时间
     * @param telephone
     * @return
     */
    ServiceResult<String>  sendSms(String telephone);

    /**
     * 获取验证码
     * @param telephone
     * @return
     */
    String getSmsCode(String telephone);

    /**
     * 移除验证码
     */
    void remove(String telephone);
}
