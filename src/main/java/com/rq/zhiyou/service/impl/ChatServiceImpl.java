package com.rq.zhiyou.service.impl;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rq.zhiyou.common.StatusCode;
import com.rq.zhiyou.exception.BusinessException;
import com.rq.zhiyou.mapper.ChatMapper;
import com.rq.zhiyou.model.domain.Chat;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.vo.ChatVO;
import com.rq.zhiyou.model.vo.WebSocketVO;
import com.rq.zhiyou.service.ChatService;
import com.rq.zhiyou.service.UserService;
import com.rq.zhiyou.service.UserTeamService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.rq.zhiyou.constant.ChatConstant.PRIVATE_CHAT_CACHE;
import static com.rq.zhiyou.constant.ChatConstant.TEAM_CHAT_CACHE;

/**
* @author 若倾
* @description 针对表【chat(聊天消息表)】的数据库操作Service实现
* @createDate 2023-06-09 19:43:12
*/
@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat>
    implements ChatService {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public List<ChatVO> getPrivateChat(Long toId, User loginUser) {
        if (toId==null||toId <=0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        long userId = loginUser.getId();
        List<ChatVO> chatCache = getCache(PRIVATE_CHAT_CACHE, userId + "-" + toId);
        if (chatCache!=null){
            //续期
            saveCache(PRIVATE_CHAT_CACHE, userId + "-" + toId,chatCache);
            return chatCache;
        }
        LambdaQueryWrapper<Chat> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Chat::getFromId,userId).eq(Chat::getToId,toId)
                .or()
                .eq(Chat::getFromId,toId).eq(Chat::getToId,userId);
        List<Chat> list = list(queryWrapper);
        List<ChatVO> chatVOList = list.stream().map(chat -> {
            ChatVO chatVO  = getChatVO(userId, chat);
            return chatVO;
        }).collect(Collectors.toList());
        saveCache(PRIVATE_CHAT_CACHE, userId + "-" + toId,chatVOList);
        return chatVOList;
    }

    @Override
    public List<ChatVO> getTeamChat(Long teamId, User loginUser) {
        if (teamId==null||teamId<=0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        List<ChatVO> teamCache = getCache(TEAM_CHAT_CACHE, String.valueOf(teamId));
        if (teamCache!=null){
            saveCache(TEAM_CHAT_CACHE, String.valueOf(teamId),teamCache);
            return teamCache;
        }
        long userId = loginUser.getId();
        LambdaQueryWrapper<Chat> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Chat::getTeamId,teamId);
        List<Chat> chatList = list(queryWrapper);
        List<ChatVO> chatVOList = chatList.stream().map(chat -> {
            ChatVO chatVO = getChatVO(userId, chat);
            return chatVO;
        }).collect(Collectors.toList());
        saveCache(TEAM_CHAT_CACHE, String.valueOf(teamId),chatVOList);
        return chatVOList;
    }

    @Override
    public ChatVO chatResult(Long userId, String text, Integer chatType) {
        ChatVO chatVO = new ChatVO();
        WebSocketVO fromUser = new WebSocketVO();
        BeanUtils.copyProperties(userService.getById(userId), fromUser);
        chatVO.setSendMsgUser(fromUser);
        chatVO.setChatType(chatType);
        chatVO.setCreateTime(DateUtil.format(LocalDateTime.now(), "yyyy年MM月dd日 HH:mm:ss"));
        if (fromUser.getId().equals(userId)) {
            chatVO.setIsMy(true);
        }
        return chatVO;
    }

    /**
        Chat VO映射
    */
    private ChatVO getChatVO(long userId, Chat chat) {
        ChatVO chatVO = new ChatVO();
        BeanUtils.copyProperties(chat, chatVO);
        WebSocketVO fromUser = new WebSocketVO();
        BeanUtils.copyProperties(userService.getById(chat.getFromId()), fromUser);
        chatVO.setSendMsgUser(fromUser);
        chatVO.setCreateTime(DateUtil.format(LocalDateTime.now(), "yyyy年MM月dd日 HH:mm:ss"));
        if (fromUser.getId() == userId) {
            chatVO.setIsMy(true);
        }
        return chatVO;
    }

    /**
     * 获取缓存
    */
    public List<ChatVO> getCache(String keyPrefix,String keySuffix){
        return (List<ChatVO>) redisTemplate.opsForValue().get(keyPrefix + keySuffix);
    }
    /**
     * 删除缓存
     */
    public void deleteKey(String keyPrefix,String keySuffix){
        redisTemplate.delete(keyPrefix+keySuffix);
    }
    /**
     * 保存缓存
     */
    public void saveCache(String keyPrefix,String keySuffix,List<ChatVO> chatVOList){
        //设置随机TTL 解决缓存雪崩
        int time = RandomUtil.randomInt(2, 4);
        try {
            redisTemplate.opsForValue().set(keyPrefix+keySuffix,chatVOList,2+time/5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis set cache error!");
        }
    }


}




