use zhiyou;
CREATE TABLE user (
      id bigint NOT NULL AUTO_INCREMENT,
      username varchar(256) DEFAULT NULL COMMENT '用户昵称',
      account varchar(256) DEFAULT NULL COMMENT '账号',
      avatar_url varchar(1024) DEFAULT NULL COMMENT '用户头像',
      gender tinyint DEFAULT NULL COMMENT '性别',
      profile varchar(512) DEFAULT NULL COMMENT '用户简介',
      password varchar(512) NOT NULL COMMENT '密码',
      friend_ids varchar(512)  NULL COMMENT '朋友id列表',
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

create table team
(
    id           bigint auto_increment comment 'id' primary key,
    team_avatar_url varchar(1024)                      null comment '队伍头像',
    name   varchar(256)                   not null comment '队伍名称',
    description varchar(1024)                      null comment '描述',
    max_num    int      default 1                 not null comment '最大人数',
    expire_time    datetime  null comment '过期时间',
    user_id            bigint comment '用户id',
    status    int      default 0                 not null comment '0 - 公开，1 - 私有，2 - 加密',
    password varchar(512)                       null comment '密码',
    announce      varchar(512)                       null comment '队伍公告',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_delete     tinyint  default 0                 not null comment '是否删除'
) comment '队伍';

create table user_team
(
    id           bigint auto_increment comment 'id'
        primary key,
    user_id            bigint comment '用户id',
    team_id            bigint comment '队伍id',
    join_time datetime  null comment '加入时间',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_delete     tinyint  default 0                 not null comment '是否删除'
) comment '用户队伍关系';

create table friends
(
    id         bigint auto_increment comment '好友申请id'
        primary key,
    from_id     bigint                             not null comment '发送申请的用户id',
    receive_id  bigint                             null comment '接收申请的用户id ',
    is_read     tinyint  default 0                 not null comment '是否已读(0-未读 1-已读)',
    status     tinyint  default 0                 not null comment '申请状态 默认0 （0-未通过 1-已同意 2-不同意）',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP null,
    is_delete   tinyint  default 0                 not null comment '是否删除',
    remark     varchar(256)                       null comment '好友申请备注信息'
)
    comment '好友申请管理表' charset = utf8mb4;

create table chat
(
    id         bigint auto_increment comment '聊天记录id'
        primary key,
    from_id     bigint                                  not null comment '发送消息id',
    to_id       bigint                                  null comment '接收消息id',
    text       varchar(512)                             null comment '聊天记录',
    chat_type   tinyint                                 not null comment '聊天类型 1-私聊 2-群聊',
    team_id     bigint                                  null comment '队伍id',
    create_time datetime default CURRENT_TIMESTAMP      null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP  null comment '修改时间'
)
    comment '聊天消息表' charset = utf8mb4;

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

