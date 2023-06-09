package com.rq.zhiyou.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rq.zhiyou.mapper.ChatMapper;
import com.rq.zhiyou.model.domain.Chat;
import com.rq.zhiyou.service.ChatService;
import org.springframework.stereotype.Service;

/**
* @author 若倾
* @description 针对表【chat(聊天消息表)】的数据库操作Service实现
* @createDate 2023-06-09 19:43:12
*/
@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat>
    implements ChatService {

}




