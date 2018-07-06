package com.yaoyao.online.repository;

import com.yaoyao.online.entity.HouseDetail;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 12:30
 * @Description:
 */
public interface HouseDetailRepository extends CrudRepository<HouseDetail,Long> {

    HouseDetail findByHouseId(Long houseId);

    List<HouseDetail> findAllByHouseIdIn(List<Long> houseIds);
}
