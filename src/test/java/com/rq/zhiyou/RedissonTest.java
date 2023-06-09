package com.rq.zhiyou;

import cn.hutool.core.util.RandomUtil;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/5/20 17:26
 */
@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void testRedisson() {
        RList<Object> list = redissonClient.getList("list-test");
        list.add("123");
        System.out.println(list.get(0));
    }

}
