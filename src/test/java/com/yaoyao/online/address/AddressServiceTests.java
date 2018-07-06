package com.yaoyao.online.address;

import com.yaoyao.online.HouseonlineApplicationTests;
import com.yaoyao.online.base.ServiceResult;
import com.yaoyao.online.service.IAddressService;
import com.yaoyao.online.web.Form.BaiduMapLocation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/27 15:24
 * @Description:
 */
public class AddressServiceTests extends HouseonlineApplicationTests {

    @Autowired
    private IAddressService addressService;

    @Test
    public void TestGetBaiduMapLocation(){
        String city = "北京";
        String address = "北京市昌平区巩华家园1号楼2单元";
        ServiceResult<BaiduMapLocation> baiduMapLocation = addressService.getBaiduMapLocation
                (city, address);
        System.out.println(baiduMapLocation.getResult().getLatitude());
        System.out.println(baiduMapLocation.getResult().getLongitude());
    }
}
