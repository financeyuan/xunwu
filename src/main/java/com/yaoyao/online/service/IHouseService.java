package com.yaoyao.online.service;

import com.yaoyao.online.DTO.HouseDTO;
import com.yaoyao.online.DTO.HouseSubscribeDTO;
import com.yaoyao.online.base.ServiceMultiResult;
import com.yaoyao.online.base.ServiceResult;
import com.yaoyao.online.base.SubscribeEnum;
import com.yaoyao.online.entity.HouseSubscribe;
import com.yaoyao.online.web.Form.DataTableSearch;
import com.yaoyao.online.web.Form.HouseForm;
import com.yaoyao.online.web.Form.MapSearch;
import com.yaoyao.online.web.Form.RentSearch;
import org.springframework.data.util.Pair;

import java.util.Date;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 13:44
 * @Description:房屋管理接口
 */
public interface IHouseService {

    /**
     * 新增
     * @param houseForm
     * @return
     */
    ServiceResult<HouseDTO> save(HouseForm houseForm);

    ServiceMultiResult<HouseDTO> adminQuery(DataTableSearch searchBody);

    /**
     * 查询完整房屋信息
     * @param id
     * @return
     */
    ServiceResult<HouseDTO> findCompleteOne(Long id);

    ServiceResult Update(HouseForm houseForm);

    /**
     * 移除图片
     * @param id
     * @return
     */
    ServiceResult removePhoto(Long id);

    /**
     * 修改封面
     * @param coverId
     * @param targetId
     * @return
     */
    ServiceResult updateCover(Long coverId, Long targetId);

    /**
     * 添加tag
     * @param houseId
     * @param tag
     * @return
     */
    ServiceResult addTag(Long houseId, String tag);

    /**
     * 移除tag
     * @param houseId
     * @param tag
     * @return
     */
    ServiceResult removeTag(Long houseId, String tag);

    /**
     * 更新房源状态
     * @param id
     * @param status
     * @return
     */
    ServiceResult updateStatus(Long id,int status);

    /**
     * 查询房源信息集
     * @param rentSearch
     * @return
     */
    ServiceMultiResult<HouseDTO> query(RentSearch rentSearch);

    /**
     *全地图查询
     */
    ServiceMultiResult<HouseDTO> wholeMapQuery(MapSearch mapSearch);

    /**
     * 精确范围数据查询
     * @param mapSearch
     * @return
     */
    ServiceMultiResult<HouseDTO> boundMapQuery(MapSearch mapSearch);

    /**
     * 加入预约清单
     * @param houseId
     * @return
     */
    ServiceResult  addSubscribeOrder(Long houseId);

    /**
     * 获取对应状态的预约列表
     */
    ServiceMultiResult<Pair<HouseDTO,HouseSubscribeDTO>> querySubscribeList(SubscribeEnum status, int
            start, int size);

    /**
     * 预约看房时间
     * @param houseId
     * @param orderTime
     * @param telephone
     * @param desc
     * @return
     */
    ServiceResult subscribe(Long houseId, Date orderTime,String telephone,String desc);

    /**
     * 取消预约
     * @param houseId
     * @return
     */
    ServiceResult cancelSubscribe(Long houseId);

    /**
     * 管理员查询预约信息接口
     * @param start
     * @param size
     * @return
     */
    ServiceMultiResult<Pair<HouseDTO,HouseSubscribeDTO>> findSubscribeList(int start, int size);

    /**
     * 完成预约
     * @param houseId
     */
    ServiceResult finishSubscribe(Long houseId);
}
