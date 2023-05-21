package com.rq.zhiyou.service.impl;


import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rq.zhiyou.common.StatusCode;
import com.rq.zhiyou.exception.BusinessException;
import com.rq.zhiyou.exception.ThrowUtils;
import com.rq.zhiyou.mapper.CommentMapper;
import com.rq.zhiyou.model.domain.Comment;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.vo.CommentVO;
import com.rq.zhiyou.service.CommentService;
import com.rq.zhiyou.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 若倾
* @description 针对表【comment(评论)】的数据库操作Service实现
* @createDate 2023-04-03 19:29:23
*/
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService {

    @Autowired
    private UserService userService;

    @Override
    public void validComment(Comment comment, boolean add) {
        if (comment == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        String content = comment.getContent();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(content), StatusCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(content) && content.length() > 200) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "评论过长");
        }
    }

    @Override
    public Page<CommentVO> getCommentVOPage(Page<Comment> commentPage, HttpServletRequest request) {
        List<Comment> commentList = commentPage.getRecords();
        Page<CommentVO> commentVOPage = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        if (CollectionUtils.isEmpty(commentList)) {
            return commentVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = commentList.stream().map(Comment::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        List<CommentVO> commentVOList = commentList.stream().map(comment -> {
            CommentVO commentVO = new CommentVO();
            BeanUtils.copyProperties(comment,commentVO);
            Long userId = comment.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            commentVO.setUser(userService.getUserVO(user));
            return commentVO;
        }).collect(Collectors.toList());
        commentVOPage.setRecords(commentVOList);
        return commentVOPage;
    }

}




