package com.rq.zhiyou.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rq.zhiyou.model.domain.Comment;
import com.rq.zhiyou.model.vo.CommentVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 若倾
* @description 针对表【comment(评论)】的数据库操作Service
* @createDate 2023-04-03 19:29:23
*/
public interface CommentService extends IService<Comment> {
    /**
     * 校验
     *
     * @param comment
     * @param add
     */
    void validComment(Comment comment, boolean add);
    /**
     * 分页获取评论封装
     *
     * @param commentPage
     * @param request
     * @return
     */
    Page<CommentVO> getCommentVOPage(Page<Comment> commentPage, HttpServletRequest request);
}
