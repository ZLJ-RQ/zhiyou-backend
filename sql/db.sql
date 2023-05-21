use zhiyou;
CREATE TABLE user (
      id bigint NOT NULL AUTO_INCREMENT,
      username varchar(256) DEFAULT NULL COMMENT '用户昵称',
      account varchar(256) DEFAULT NULL COMMENT '账号',
      avatar_url varchar(1024) DEFAULT NULL COMMENT '用户头像',
      gender tinyint DEFAULT NULL COMMENT '性别',
      profile varchar(512) DEFAULT NULL COMMENT '用户简介',
      password varchar(512) NOT NULL COMMENT '密码',
      email varchar(512) DEFAULT NULL COMMENT '邮箱',
      status int DEFAULT '0' COMMENT '状态 0-正常',
      phone varchar(128) DEFAULT NULL COMMENT '电话',
      user_role int NOT NULL DEFAULT '0' COMMENT '用户角色(0-普通用户 1-管理员)',
      tags      varchar(1024) null comment '标签 json 列表',
      create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
      is_delete tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
          PRIMARY KEY (id),
      UNIQUE KEY `account` (account)
)  COMMENT='用户表';

create table tag
(
    id         bigint auto_increment comment 'id'
        primary key,
    tag_name    varchar(256) null comment '标签名称',
    user_id     bigint null comment '用户 id',
    parent_id   bigint null comment '父标签 id',
    is_parent   tinyint null comment '0 - 不是, 1 - 父标签',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_delete   tinyint  default 0 not null comment '是否删除',
    constraint uniIdx_tagName
        unique (tag_name)
) comment '标签';

