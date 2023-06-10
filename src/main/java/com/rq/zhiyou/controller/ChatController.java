package com.rq.zhiyou.controller;

import com.rq.zhiyou.common.StatusCode;
import com.rq.zhiyou.exception.BusinessException;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.request.chat.PrivateChatRequest;
import com.rq.zhiyou.model.request.chat.TeamChatRequest;
import com.rq.zhiyou.model.vo.ChatVO;
import com.rq.zhiyou.service.ChatService;
import com.rq.zhiyou.service.UserService;
import com.rq.zhiyou.utils.ResultData;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 若倾
 * @description TODO
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ChatService chatService;

    @Resource
    private UserService userService;



    @PostMapping("/privateChat")
    public ResultData<List<ChatVO>> getPrivateChat(@RequestBody PrivateChatRequest privateChatRequest, HttpServletRequest request){
        if(privateChatRequest==null){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        Long toId = privateChatRequest.getToId();
        if (toId==null||toId <=0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<ChatVO> chatVOList=chatService.getPrivateChat(toId,loginUser);
        return ResultData.success(chatVOList);
    }

    @PostMapping("/teamChat")
    public ResultData<List<ChatVO>> getTeamChat(@RequestBody TeamChatRequest teamChatRequest, HttpServletRequest request){
        if(teamChatRequest==null){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        Long teamId = teamChatRequest.getTeamId();
        if (teamId==null||teamId <=0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<ChatVO> chatVOList=chatService.getTeamChat(teamId,loginUser);
        return ResultData.success(chatVOList);
    }

}
