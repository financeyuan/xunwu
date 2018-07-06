package com.yaoyao.online.seach;

import com.yaoyao.online.HouseonlineApplicationTests;
import com.yaoyao.online.base.ServiceMultiResult;
import com.yaoyao.online.base.ServiceResult;
import com.yaoyao.online.service.ISearchService;
import com.yaoyao.online.web.Form.RentSearch;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/13 14:38
 * @Description:
 */
public class ESSearchTest extends HouseonlineApplicationTests {

    @Autowired
    private ISearchService searchService;

    @Test
    public void TestIndex() {
         searchService.index(15L);
    }

    @Test
    public void TestRemove() {
        searchService.remove(15L);
    }


    @Test
    public void TestQuery(){
        RentSearch rentSearch = new RentSearch();
        rentSearch.setCityEnName("bj");
        rentSearch.setStart(0);
        rentSearch.setSize(10);
        rentSearch.setKeywords("国贸");
        ServiceMultiResult<Long> serviceResult = searchService.query(rentSearch);
        System.out.print(serviceResult.getResult().toString());
        Assert.assertTrue(serviceResult.getTotal() > 0);
    }

    @Test
    public void TestSearchForYou(){
        ServiceResult<List<String>> suggest = searchService.suggest("二");
        for (int i = 0; i < suggest.getResult().size(); i++) {
            System.out.println(suggest.getResult().get(i));
        }
    }
}
