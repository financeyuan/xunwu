package com.yaoyao.online.repository;

import com.yaoyao.online.entity.Subway;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 12:37
 * @Description:
 */
public interface SubwayRepository extends CrudRepository<Subway,Long> {
    List<Subway> findAllByCityEnName(String cityEnName);
}
