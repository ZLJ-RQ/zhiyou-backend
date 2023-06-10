package com.rq.zhiyou.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.rq.zhiyou.model.domain.Chat;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.vo.ChatVO;

import java.util.List;

/**
* @author 若倾
* @description 针对表【chat(聊天消息表)】的数据库操作Service
* @createDate 2023-06-09 19:43:12
*/
public interface ChatService extends IService<Chat> {

    List<ChatVO> getPrivateChat(Long toId, User loginUser);

    List<ChatVO> getTeamChat(Long teamId, User loginUser);

    ChatVO chatResult(Long userId, String text, Integer chatType);

    /**
     * 获取缓存
     */
    List<ChatVO> getCache(String keyPrefix,String keySuffix);
    /**
     * 删除缓存
     */
    void deleteKey(String keyPrefix,String keySuffix);
    /**
     * 保存缓存
     */
    void saveCache(String keyPrefix,String keySuffix,List<ChatVO> chatVOList);
}
