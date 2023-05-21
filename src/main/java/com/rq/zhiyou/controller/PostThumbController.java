package com.rq.zhiyou.controller;

import com.rq.zhiyou.common.StatusCode;
import com.rq.zhiyou.exception.BusinessException;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.dto.postthumb.PostThumbAddRequest;
import com.rq.zhiyou.service.PostThumbService;
import com.rq.zhiyou.service.UserService;
import com.rq.zhiyou.utils.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子点赞接口
 *

 */
@RestController
@RequestMapping("/post_thumb")
@Slf4j
public class PostThumbController {

    @Resource
    private PostThumbService postThumbService;

    @Resource
    private UserService userService;

    /**
     * 点赞 / 取消点赞
     *
     * @param postThumbAddRequest
     * @param request
     * @return resultNum 本次点赞变化数
     */
    @PostMapping("/")
    public ResultData<Integer> doThumb(@RequestBody PostThumbAddRequest postThumbAddRequest,
                                       HttpServletRequest request) {
        if (postThumbAddRequest == null || postThumbAddRequest.getPostId() <= 0) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long postId = postThumbAddRequest.getPostId();
        int result = postThumbService.doPostThumb(postId, loginUser);
        return ResultData.success(result);
    }

}
