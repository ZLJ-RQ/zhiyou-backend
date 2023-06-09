package com.rq.zhiyou.once;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@Component
@Slf4j
@ServerEndpoint(value = "/socket/{userId}")
public class TestSocket {
    @OnOpen
    public void onOpen(@PathParam("userId") String userId) {
        log.info("ChatWebsocket open 有新连接加入 userId: {}", userId);
        log.info("ChatWebsocket open 连接建立完成 userId: {}", userId);
    }
    @OnClose
    public void OnClose() {
        log.info("连接断开");
    }

    @OnError
    public void OnError(Throwable error) {
        log.error("发生了错误  errorMessage: {}", error.getMessage());
    }

    @OnMessage
    public void OnMessage(String message) {
        log.info("收到消息：{}", message);
    }
}