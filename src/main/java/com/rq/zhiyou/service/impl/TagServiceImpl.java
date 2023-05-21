package com.rq.zhiyou.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rq.zhiyou.mapper.TagMapper;
import com.rq.zhiyou.model.domain.Tag;
import com.rq.zhiyou.service.TagService;
import org.springframework.stereotype.Service;

/**
* @author 若倾
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2023-05-14 17:15:41
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService {

}




