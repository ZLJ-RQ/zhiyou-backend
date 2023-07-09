package com.rq.zhiyou.model.vo;

import com.rq.zhiyou.model.domain.Tag;
import lombok.Data;

import java.util.List;

/**
 * @author 若倾
 * @description TODO
 */
@Data
public class TagVO extends Tag {
    List<Tag> childrenTreeNodes;
}
