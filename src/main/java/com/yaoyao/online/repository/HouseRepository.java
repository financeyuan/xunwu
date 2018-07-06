package com.yaoyao.online.repository;

import com.yaoyao.online.entity.House;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.transaction.Transactional;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 12:29
 * @Description:
 */
public interface HouseRepository extends PagingAndSortingRepository<House,Long>,
        JpaSpecificationExecutor<House> {

    @Modifying
    @Query("update House as house set house.cover = :cover where house.id = :id")
    void updateCover(@Param(value = "id") Long id, @Param(value = "cover")String path);

    @Modifying
    @Query("update House as house set house.status = :status where house.id = :id")
    void updateStatus(@Param(value = "id") Long id, @Param(value = "status")int status);

    @Modifying
    @Query("update House as house set house.watchTimes = house.watchTimes+1 where house.id = :id")
    void updateWatchTimes(@Param(value = "id")Long houseId);
}
