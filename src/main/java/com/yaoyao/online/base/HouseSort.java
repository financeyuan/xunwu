package com.yaoyao.online.base;

import com.google.common.collect.Sets;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/8 14:59
 * @Description:
 */
public class HouseSort {

    public static final String DEFAULT_SORT_KEY = "lastUpdateTime";

    public static final String DISTANCE_TO_SUBWAY_KEY = "distanceToSubway";

    public static final Set<String> SORT_KEY = Sets.newHashSet(
            DEFAULT_SORT_KEY,
            "createTime",
            "price",
            "area",
            DISTANCE_TO_SUBWAY_KEY
    );

    public static Sort generateSort(String key, String directionKey) {
        key = getSortKey(key);
        Sort.Direction direction = Sort.Direction.fromStringOrNull(directionKey);
        if (direction == null) {
            direction = Sort.Direction.DESC;
        }
        return new Sort(direction, key);
    }

    public static String getSortKey(String key) {
        if (!SORT_KEY.contains(key)) {
            key = DEFAULT_SORT_KEY;
        }
        return key;
    }
}
