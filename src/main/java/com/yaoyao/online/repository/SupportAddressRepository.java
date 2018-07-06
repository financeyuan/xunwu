package com.yaoyao.online.repository;

import com.yaoyao.online.entity.SupportAddress;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 10:53
 * @Description:SupportAddressRepository
 */
public interface SupportAddressRepository extends CrudRepository<SupportAddress,Long> {

    /**
     * 获取所有行政级别信息
     * @param level
     * @return
     */
    List<SupportAddress> findAllByLevel(String level);

    SupportAddress findByEnNameAndLevel(String enName, String level);

    SupportAddress findByEnNameAndBelongTo(String enName, String belongTo);

    List<SupportAddress> findAllByLevelAndBelongTo(String level, String belongTo);
}
