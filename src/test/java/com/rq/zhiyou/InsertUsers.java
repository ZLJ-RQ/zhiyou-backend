package com.rq.zhiyou;

import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/5/19 14:11
 */
@SpringBootTest
public class InsertUsers {

    @Resource
    private UserService userService;

    private ExecutorService executorService=new ThreadPoolExecutor(40,100,60, TimeUnit.SECONDS,new ArrayBlockingQueue<>(10000));


    //批量插入用户
    @Test
    void insertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM=100000;
        List<User> list = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("zhiyou");
            user.setAccount("zhiyoub"+i);
            user.setAvatarUrl("https://img0.baidu.com/it/u=1340932756,1533495834&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500");
            user.setGender(0);
            user.setProfile("这是我的自我介绍");
            user.setPassword("12345678");
            user.setEmail("123@qq.com");
            user.setStatus(0);
            user.setPhone("123");
            user.setTags("[\"java\",\"男\",\"python\"]");
            user.setUserRole(0);
            list.add(user);
        }
        userService.saveBatch(list,1000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    //并发批量插入用户
    @Test
    void doConcurrencyInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int batchSize=2500;
        int j=0;
        List<CompletableFuture<Void>> futureList=new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            List<User> list = new ArrayList<>();
            while (true){
                j++;
                User user = new User();
                user.setUsername("zhiyou");
                user.setAccount("zhiyoug"+j);
                user.setAvatarUrl("https://img0.baidu.com/it/u=1340932756,1533495834&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500");
                user.setGender(0);
                user.setProfile("这是我的自我介绍");
                user.setPassword("12345678");
                user.setEmail("123@qq.com");
                user.setStatus(0);
                user.setPhone("123");
                user.setTags("[\"java\",\"男\",\"python\"]");
                user.setUserRole(0);
                list.add(user);
                if (j%batchSize==0){
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                userService.saveBatch(list, 1000);
            },executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
