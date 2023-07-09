package com.rq.zhiyou.controller;

import cn.hutool.core.util.ObjectUtil;
import com.rq.zhiyou.common.DeleteRequest;
import com.rq.zhiyou.common.StatusCode;
import com.rq.zhiyou.exception.BusinessException;
import com.rq.zhiyou.model.domain.Tag;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.request.chat.PrivateChatRequest;
import com.rq.zhiyou.model.request.chat.TeamChatRequest;
import com.rq.zhiyou.model.request.tag.TagAddRequest;
import com.rq.zhiyou.model.request.tag.TagUpdateRequest;
import com.rq.zhiyou.model.vo.ChatVO;
import com.rq.zhiyou.model.vo.TagVO;
import com.rq.zhiyou.service.ChatService;
import com.rq.zhiyou.service.TagService;
import com.rq.zhiyou.service.UserService;
import com.rq.zhiyou.utils.ResultData;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 若倾
 * @description TODO
 */
@RestController
@RequestMapping("/tag")

public class TagController {

    @Resource
    private UserService userService;

    @Resource
    private TagService tagService;

    @GetMapping("/tree")
    public ResultData<TagVO> selectTreeNodes(Long id) {
        List<TagVO> list = tagService.selectTreeNodes(id);
        return ResultData.success(list);
    }

    @PostMapping("/add")
    public ResultData<Boolean> addTag(@RequestBody TagAddRequest tagAddRequest,HttpServletRequest request){
        if (ObjectUtil.isEmpty(tagAddRequest)){
            return ResultData.error(StatusCode.NULL_ERROR,"请输入数据");
        }
        if (!userService.isAdmin(request)) {
            return ResultData.error(StatusCode.NO_AUTH,"只有管理员可以操作");
        }
        boolean res =tagService.addTag(tagAddRequest);
        return ResultData.success(res);
    }

    @PostMapping("/remove")
    public ResultData<Boolean> removeTag(@RequestBody DeleteRequest deleteRequest,HttpServletRequest request){
        if (ObjectUtil.isEmpty(deleteRequest)){
            return ResultData.error(StatusCode.NULL_ERROR,"请输入数据");
        }
        if (!userService.isAdmin(request)) {
            return ResultData.error(StatusCode.NO_AUTH,"只有管理员可以操作");
        }
        boolean res =tagService.removeTag(deleteRequest.getId());
        return ResultData.success(res);
    }

    @PostMapping("/update")
    public ResultData<Boolean> updateTag(@RequestBody TagUpdateRequest tagUpdateRequest,HttpServletRequest request){
        if (ObjectUtil.isEmpty(tagUpdateRequest)){
            return ResultData.error(StatusCode.NULL_ERROR,"请输入数据");
        }
        if (!userService.isAdmin(request)) {
            return ResultData.error(StatusCode.NO_AUTH,"只有管理员可以操作");
        }
        boolean res =tagService.updateTag(tagUpdateRequest);
        return ResultData.success(res);
    }


}
