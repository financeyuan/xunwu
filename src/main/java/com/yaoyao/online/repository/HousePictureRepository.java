package com.yaoyao.online.repository;

import com.yaoyao.online.entity.HousePicture;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/6 12:35
 * @Description:
 */
public interface HousePictureRepository extends CrudRepository<HousePicture,Long> {

    List<HousePicture> findAllByHouseId(Long id);

}
