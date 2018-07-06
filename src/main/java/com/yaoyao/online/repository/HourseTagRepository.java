package com.yaoyao.online.repository;

import com.yaoyao.online.entity.HouseTag;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 12:36
 * @Description:
 */
public interface HourseTagRepository extends CrudRepository<HouseTag,Long> {

    HouseTag findByNameAndHouseId(String name, Long houseId);

    List<HouseTag> findAllByHouseId(Long id);

    List<HouseTag> findAllByHouseIdIn(List<Long> houseIds);

}
