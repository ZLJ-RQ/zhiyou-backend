package com.rq.zhiyou.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rq.zhiyou.common.StatusCode;
import com.rq.zhiyou.exception.BusinessException;
import com.rq.zhiyou.mapper.TagMapper;
import com.rq.zhiyou.model.domain.Tag;
import com.rq.zhiyou.model.request.tag.TagAddRequest;
import com.rq.zhiyou.model.request.tag.TagUpdateRequest;
import com.rq.zhiyou.model.vo.TagVO;
import com.rq.zhiyou.service.TagService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
* @author 若倾
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2023-07-09 09:45:10
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService {

    @Resource
    private TagMapper tagMapper;

    @Override
    public List<TagVO> selectTreeNodes(Long id) {
        //查询所有的数据
        List<TagVO> tagVOS = tagMapper.selectTreeNodes(id);
        List<TagVO> list = new ArrayList<>();
        //先提取出当前父节点下的子节点数据
        HashMap<Long, TagVO> map = new HashMap<>();
        tagVOS.stream().forEach(tagVO -> {
            map.put(tagVO.getId(),tagVO);
            if (tagVO.getParentId().equals(id)){
                list.add(tagVO);
            }
            Long parentId = tagVO.getParentId();
            TagVO parentNodes = map.get(parentId);
            if (parentNodes!=null){
                List<Tag> childrenTreeNodes = parentNodes.getChildrenTreeNodes();
                if (childrenTreeNodes==null){
                    ArrayList<Tag> tags = new ArrayList<>();
                    parentNodes.setChildrenTreeNodes(tags);
                }
                parentNodes.getChildrenTreeNodes().add(tagVO);
            }
        });
        return list;
    }

    @Override
    public boolean addTag(TagAddRequest tagAddRequest) {
        String tagName = tagAddRequest.getTagName();
        if (StrUtil.isBlank(tagName)||tagName.length()>15){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"标签不存在或过长");
        }
        Long parentId = tagAddRequest.getParentId();
        if (parentId==null||parentId<0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        Tag tag = new Tag();
        BeanUtil.copyProperties(tagAddRequest,tag);
        return save(tag);
    }

    @Override
    public boolean removeTag(Long id) {
        if (id==null||id<0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        return removeById(id);
    }

    @Override
    public boolean updateTag(TagUpdateRequest tagUpdateRequest) {
        Long id = tagUpdateRequest.getId();
        String tagName = tagUpdateRequest.getTagName();
        if (id==null||id<0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        if (StrUtil.isBlank(tagName)||tagName.length()>15){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"标签不存在或过长");
        }
        Tag tag = new Tag();
        BeanUtil.copyProperties(tagUpdateRequest,tag);
        return updateById(tag);
    }
}




