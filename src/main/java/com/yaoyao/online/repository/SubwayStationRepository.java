package com.yaoyao.online.repository;

import com.yaoyao.online.entity.SubwayStation;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 12:38
 * @Description:
 */
public interface SubwayStationRepository extends CrudRepository<SubwayStation,Long> {
    List<SubwayStation> findAllBySubwayId(Long subwayId);
}
