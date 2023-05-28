package com.rq.zhiyou;

import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.service.UserService;
import com.rq.zhiyou.utils.AlgorithmUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;


@SpringBootTest
@RunWith(SpringRunner.class)
class ZhiYouApplicationTests {




    @Test
    void contextLoads() {
        List<String> list1 = Arrays.asList("男", "java", "c++", "python");
        List<String> list2 = Arrays.asList("java", "c++", "女", "python");
        System.out.println(AlgorithmUtils.minDistance(list1, list2));
    }
//    @Autowired
//    MinioClient minioClient;

    @Autowired
    UserService userService;

//    @Test
//    public void upload(){
//        try {
//            minioClient.uploadObject(
//                    UploadObjectArgs.builder()
//                            .bucket("fsp-dev")
//                            .object("txt/1.txt")
//                            .filename("E:\\Desktop\\1.txt")
//                            .build());
//            System.out.println("上传成功了");
//        }catch (Exception e){
//            e.printStackTrace();
//            System.out.println("上传失败");
//        }
//    }

    @Test
    public void searchUserByTags(){
        List<String> list= Arrays.asList("java","python");
        List<User> users = userService.searchUserByTags(list);
        Assert.assertNotNull(users);
    }

}
