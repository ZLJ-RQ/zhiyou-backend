package com.rq.zhiyou.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rq.zhiyou.common.StatusCode;
import com.rq.zhiyou.exception.BusinessException;
import com.rq.zhiyou.exception.ThrowUtils;
import com.rq.zhiyou.model.domain.Post;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.dto.post.PostQueryRequest;
import com.rq.zhiyou.model.dto.postfavour.PostFavourAddRequest;
import com.rq.zhiyou.model.dto.postfavour.PostFavourQueryRequest;
import com.rq.zhiyou.model.vo.PostVO;
import com.rq.zhiyou.service.PostFavourService;
import com.rq.zhiyou.service.PostService;
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
 * 帖子收藏接口
 *

 */
@RestController
@RequestMapping("/post_favour")
@Slf4j
public class PostFavourController {

    @Resource
    private PostFavourService postFavourService;

    @Resource
    private PostService postService;

    @Resource
    private UserService userService;

    /**
     * 收藏 / 取消收藏
     *
     * @param postFavourAddRequest
     * @param request
     * @return resultNum 收藏变化数
     */
    @PostMapping("/")
    public ResultData<Integer> doPostFavour(@RequestBody PostFavourAddRequest postFavourAddRequest,
                                            HttpServletRequest request) {
        if (postFavourAddRequest == null || postFavourAddRequest.getPostId() <= 0) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        // 登录才能操作
        final User loginUser = userService.getLoginUser(request);
        long postId = postFavourAddRequest.getPostId();
        int result = postFavourService.doPostFavour(postId, loginUser);
        return ResultData.success(result);
    }

    /**
     * 获取我收藏的帖子列表
     *
     * @param postQueryRequest
     * @param request
     */
    @PostMapping("/my/list/page")
    public ResultData<Page<PostVO>> listMyFavourPostByPage(@RequestBody PostQueryRequest postQueryRequest,
                                                             HttpServletRequest request) {
        if (postQueryRequest == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, StatusCode.PARAMS_ERROR);
        Page<Post> postPage = postFavourService.listFavourPostByPage(new Page<>(current, size),
                postService.getQueryWrapper(postQueryRequest), loginUser.getId());
        return ResultData.success(postService.getPostVOPage(postPage, request));
    }

    /**
     * 获取用户收藏的帖子列表
     *
     * @param postFavourQueryRequest
     * @param request
     */
    @PostMapping("/list/page")
    public ResultData<Page<PostVO>> listFavourPostByPage(@RequestBody PostFavourQueryRequest postFavourQueryRequest,
            HttpServletRequest request) {
        if (postFavourQueryRequest == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        long current = postFavourQueryRequest.getCurrent();
        long size = postFavourQueryRequest.getPageSize();
        Long userId = postFavourQueryRequest.getUserId();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20 || userId == null, StatusCode.PARAMS_ERROR);
        Page<Post> postPage = postFavourService.listFavourPostByPage(new Page<>(current, size),
                postService.getQueryWrapper(postFavourQueryRequest.getPostQueryRequest()), userId);
        return ResultData.success(postService.getPostVOPage(postPage, request));
    }
}
