package com.rq.zhiyou.constant;

/**
 * @author 若倾
 * @description TODO
 */
public interface ChatConstant {
    /**
     * 私聊
     */
    int PRIVATE_CHAT = 1;
    /**
     * 队伍群聊
     */
    int TEAM_CHAT = 2;

    /**
     * 私聊消息缓存
     */
    String PRIVATE_CHAT_CACHE="zhiyou:chat:private_chat:";

    /**
     * 队伍群聊消息缓存
     */
    String TEAM_CHAT_CACHE="zhiyou:chat:team_chat:";

}
