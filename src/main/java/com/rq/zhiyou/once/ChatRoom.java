package com.rq.zhiyou.once;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@ServerEndpoint(value = "/chatRoom/{userId}/{teamId}")
public class ChatRoom {
    /**
     * 存储每一个连接的用户和其对应的ChatRoom
     * 由于webSocket是多例的，采用static把userMap变成一个
     */
    private static Map<String, ConcurrentHashMap<String,ChatRoom>> teamUserMap = new HashMap<>();
    /**
     * 会话    import javax.websocket.Session;
     */
    private Session session;
    /**
     * userId
     */
    private String userId;
    /**
     * teamId
     */
    private String teamId;


    @OnOpen
    public void onOpen(@PathParam("userId") String userId,@PathParam("teamId")String teamId, Session session) {
        log.info("有新连接加入 userId: {}", userId);
        log.info("连接建立完成 userId: {}", userId);
        this.session = session;
        this.userId = userId;
        this.teamId=teamId;
        ConcurrentHashMap<String, ChatRoom> room = new ConcurrentHashMap<>(0);
        room.put(teamId,this);
        teamUserMap.put(userId,room);
    }

    @OnClose
    public void OnClose() {
        log.info("连接断开userId: {}", userId);
    }

    @OnError
    public void OnError(Throwable error) {
        log.error("发生了错误  errorMessage: {}", error.getMessage());
    }

    @OnMessage
    public void OnMessage(String message) throws IOException {
        log.info("收到消息：{}", message);
        sendMessage(message);
//        broadcast(teamId,message);
    }
    /**
     * 队伍内群发消息
     *
     * @param teamId
     * @param msg
     * @throws Exception
     */
//    public static void broadcast(String teamId, String msg) {
//        ConcurrentHashMap<String, ChatRoom> map = teamUserMap.get(teamId);
//        // keySet获取map集合key的集合  然后在遍历key即可
//        for (String key : map.keySet()) {
//            try {
//                ChatRoom chatRoom = map.get(key);
//                chatRoom.sendMessage(msg);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

	//聊天室发送消息
    public void sendMessage(String message) {
            for (Map.Entry<String, ConcurrentHashMap<String, ChatRoom>> entry : teamUserMap.entrySet()) {
                ConcurrentHashMap<String, ChatRoom> map = entry.getValue();
                map.forEach((userId,chatRoom)->{
                    if (!userId.equals(this.userId)){
                        chatRoom.session.getAsyncRemote().sendText(message);
                    }
                });
            }
    }
//        userMap.keySet().forEach((userId) -> {
//            if (!userId.equals(this.userId))     //不给自己发
//                userMap.get(userId).session.getAsyncRemote().sendText(message);
//        });

    /**
     * 发送消息
     *
     * @param message
     * @throws IOException
     */
//    public void sendMessage(String message) throws IOException {
//        this.session.getBasicRemote().sendText(message);
//    }

    //队伍发送消息
//    public void sendGroupMessage(String message) {
//        userMap.keySet().forEach((userId) -> {
//            if (!userId.equals(this.userId))     //不给自己发
//                userMap.get(userId).session.getAsyncRemote().sendText(message);
//        });
//    }
}