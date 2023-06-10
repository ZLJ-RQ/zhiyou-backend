package com.rq.zhiyou.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rq.zhiyou.common.DeleteRequest;
import com.rq.zhiyou.common.StatusCode;
import com.rq.zhiyou.exception.BusinessException;
import com.rq.zhiyou.exception.ThrowUtils;
import com.rq.zhiyou.model.domain.Comment;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.request.comment.CommentAddRequest;
import com.rq.zhiyou.model.request.comment.CommentQueryRequest;
import com.rq.zhiyou.model.vo.CommentVO;
import com.rq.zhiyou.service.CommentService;
import com.rq.zhiyou.service.UserService;
import com.rq.zhiyou.utils.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/4/3 19:33
 */
@RestController
@RequestMapping("/comment")
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 创建
     *
     * @param commentAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public ResultData<Long> addComment(@RequestBody CommentAddRequest commentAddRequest, HttpServletRequest request) {
        if (commentAddRequest == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Comment comment = new Comment();
        comment.setContent(commentAddRequest.getContent());
        comment.setPostId(commentAddRequest.getPostId());
        comment.setUserId(loginUser.getId());
        commentService.validComment(comment, true);
        boolean result = commentService.save(comment);
        ThrowUtils.throwIf(!result, StatusCode.OPERATION_ERROR);
        long newCommentId = comment.getId();
        return ResultData.success(newCommentId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public ResultData<Boolean> deleteComment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Comment oldComment = commentService.getById(id);
        ThrowUtils.throwIf(oldComment == null, StatusCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldComment.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(StatusCode.NO_AUTH);
        }
        boolean b = commentService.removeById(id);
        return ResultData.success(b);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param commentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public ResultData<Page<CommentVO>> listCommentVOByPage(@RequestBody CommentQueryRequest commentQueryRequest,
                                                           HttpServletRequest request) {
        long current = commentQueryRequest.getCurrent();
        long size = commentQueryRequest.getPageSize();
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id",commentQueryRequest.getPostId());
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, StatusCode.PARAMS_ERROR);
        Page<Comment> commentPage = commentService.page(new Page<>(current, size),
                queryWrapper);
        return ResultData.success(commentService.getCommentVOPage(commentPage, request));
    }

}
