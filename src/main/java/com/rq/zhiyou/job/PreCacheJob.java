package com.rq.zhiyou.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/5/19 19:23
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    private List<Long> userList= Arrays.asList(1L);

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Scheduled(cron = "0 44 19 * * *")
    public void doCacheRecommendUser(){
        String lockKey="zhiyou:precachejob:docache:lock";
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean isLock = lock.tryLock(0, 30000L, TimeUnit.MICROSECONDS);
            if (isLock){
                for (Long userId : userList) {
                    final String RECOMMEND_KEY="zhiyou:user:recommend:"+userId;
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1,20),queryWrapper);
                    userPage.setRecords(userPage.getRecords().stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList()));
                    try {
                        redisTemplate.opsForValue().set(RECOMMEND_KEY,userPage,10, TimeUnit.MINUTES);
                    } catch (Exception e) {
                        log.error("redis set key error",e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("unlock error",e);
        } finally {
            //释放自己的锁
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }

    }

}
