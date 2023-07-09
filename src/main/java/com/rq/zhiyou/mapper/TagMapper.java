package com.rq.zhiyou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rq.zhiyou.model.domain.Tag;
import com.rq.zhiyou.model.vo.TagVO;

import java.util.List;

/**
* @author 若倾
* @description 针对表【tag(标签)】的数据库操作Mapper
* @createDate 2023-07-09 09:45:10
* @Entity com.rq.zhiyou.model.domain.Tag
*/
public interface TagMapper extends BaseMapper<Tag> {
    List<TagVO> selectTreeNodes(Long id);
}




