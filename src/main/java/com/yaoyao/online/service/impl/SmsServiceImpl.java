package com.yaoyao.online.service.impl;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.google.common.base.Strings;
import com.yaoyao.online.base.ServiceResult;
import com.yaoyao.online.service.ISmsService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/28 11:28
 * @Description:
 */
@Service
public class SmsServiceImpl implements ISmsService,InitializingBean {

    @Value("${aliyun.sms.accessKey}")
    private String accessKey;

    @Value("${aliyun.sms.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.sms.template.code}")
    private String templateCode;

    private IAcsClient acsClient;

    private static  String codePrix = "SMS::CODE::CONTENT";

    private static final String[]  NUMS = {"0","1","2","3","4","5","6","7","8","9"};

    private static final Random  random = new Random();

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Override
    public ServiceResult<String> sendSms(String telephone) {
        String gapKey = "SMS::CODE::INTEVAL" + telephone;
        String result = redisTemplate.opsForValue().get(gapKey);
        if(!Strings.isNullOrEmpty(result)){
            return new ServiceResult<String>(false,"请求次数过于频繁,请稍后再试");
        }

        String code = genrateRandomSmsCode();
        String templateParam = String.format("{\"code\": \"%s\"}", code);
        //组装请求对象
        SendSmsRequest request = new SendSmsRequest();
        //使用post提交
        request.setMethod(MethodType.POST);
        request.setPhoneNumbers(telephone);
        request.setTemplateParam(templateParam);
        request.setTemplateCode(templateCode);
        request.setSignName("yaoyaoxunwu");
        boolean tag = false;
        try {
            SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
            if(sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK")) {
                tag = true;
            }
        } catch (ClientException e) {
            e.printStackTrace();
        }
        if(tag){
            redisTemplate.opsForValue().set(gapKey,code,60,TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(codePrix+telephone,code,10,TimeUnit.MINUTES);
            return ServiceResult.of(code);
        }else {
            return new ServiceResult<String>(false,"服务忙,请稍后再试");
        }
    }

    @Override
    public String getSmsCode(String telephone) {
        return this.redisTemplate.opsForValue().get(codePrix+telephone);
    }

    @Override
    public void remove(String telephone) {
        this.redisTemplate.delete(codePrix+telephone);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //设置超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        //初始化ascClient需要的几个参数
        final String product = "Dysmsapi";//短信API产品名称（短信产品名固定，无需修改）
        final String domain = "dysmsapi.aliyuncs.com";//短信API产品域名（接口地址固定，无需修改）
        //初始化ascClient,暂时不支持多region（请勿修改）
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKey,
                accessKeySecret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        this.acsClient = new DefaultAcsClient(profile);
    }

    /**
     * 验证码生成器
     * @return
     */
    private static String genrateRandomSmsCode(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0;i < 6;i++){
            int index = random.nextInt(10);
            sb.append(NUMS[index]);
        }
        return sb.toString();
    }
}
