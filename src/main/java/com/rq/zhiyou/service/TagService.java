package com.rq.zhiyou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rq.zhiyou.model.domain.Tag;
import com.rq.zhiyou.model.request.tag.TagAddRequest;
import com.rq.zhiyou.model.request.tag.TagUpdateRequest;
import com.rq.zhiyou.model.vo.TagVO;

import java.util.List;

/**
* @author 若倾
* @description 针对表【tag(标签)】的数据库操作Service
* @createDate 2023-07-09 09:45:10
*/
public interface TagService extends IService<Tag> {
    List<TagVO> selectTreeNodes(Long id);

    boolean addTag(TagAddRequest tagAddRequest);

    boolean removeTag(Long id);

    boolean updateTag(TagUpdateRequest tagUpdateRequest);
}
