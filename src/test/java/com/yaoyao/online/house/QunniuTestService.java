package com.yaoyao.online.house;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.yaoyao.online.HouseonlineApplicationTests;
import com.yaoyao.online.service.IQiNiuService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 09:56
 * @Description:
 */
public class QunniuTestService extends HouseonlineApplicationTests {

    @Autowired
    private IQiNiuService qinniuService;

    @Test
    public void TestUploadFile(){
        String fileName = "E:\\yuanworkspeace\\houseonline\\temp\\xiaoqian.jpeg";
        File file = new File(fileName);
        Assert.assertTrue(file.exists());
        try {
            Response response = qinniuService.uploadFile(file);
            Assert.assertTrue(response.isOK());

        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }
}
