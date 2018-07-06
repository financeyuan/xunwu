package com.yaoyao.online.repository;

import com.yaoyao.online.entity.HouseSubscribe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sun.rmi.runtime.Log;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 12:35
 * @Description:
 */
public interface HouseSubscribeRepository extends PagingAndSortingRepository<HouseSubscribe,Long> {

    HouseSubscribe findByHouseIdAndUserId(Long houseId, Long userId);

    Page<HouseSubscribe> findAllByUserIdAndStatus(Long userId, int status, Pageable pageable);

    Page<HouseSubscribe>  findAllByAdminIdAndStatus(Long adminId,int status, Pageable pageable);

    HouseSubscribe findByHouseIdAndAdminId(Long houseId,Long adminId);

    @Modifying
    @Query("update HouseSubscribe as subscribe set subscribe.status = :status where subscribe.id " +
            "= :id")
    void updateStatus(@Param(value = "id") Long id, @Param(value = "status") int status);
}
