package com.yaoyao.online.service;

import com.yaoyao.online.DTO.HouseBucketDTO;
import com.yaoyao.online.base.ServiceMultiResult;
import com.yaoyao.online.base.ServiceResult;
import com.yaoyao.online.web.Form.MapSearch;
import com.yaoyao.online.web.Form.RentSearch;

import java.util.List;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/13 10:54
 * @Description:检索接口
 */
public interface ISearchService {

    /**
     * 索引目标房源
     * @param houseId
     */
    void index(Long houseId);

    /**
     * 移除目标房源索引
     * @param houseId
     */
    void remove(Long houseId);

    /**
     * 查询房源接口
     * @param rentSearch
     * @return
     */
    ServiceMultiResult<Long> query(RentSearch rentSearch);

    /**
     *自动补全
     */
    ServiceResult<List<String>> suggest(String prefix);

    /**
     * 聚合小区房间数
     */
    ServiceResult<Long> aggregateDistrictHouse(String cityEnName,String regionEnName,String
            district);

    /**
     *聚合城市数据
     */
    ServiceMultiResult<HouseBucketDTO> mapAggregate(String cityEnName);

    /**
     * 城市级别查询
     * @param cityEnName
     * @param orderby
     * @param orderDirection
     * @param start
     * @param size
     * @return
     */
    ServiceMultiResult<Long> mapQuery(String cityEnName,String orderby,String orderDirection,int
            start,int size);

    /**
     * 精确范围数据查询
     * @param mapSearch
     * @return
     */
    ServiceMultiResult<Long> mapQuery(MapSearch mapSearch);
}
