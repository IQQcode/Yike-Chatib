package com.iqqcode.chatib.web;

import com.google.gson.Gson;
import com.iqqcode.chatib.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Author: iqqcode
 * @Date: 2020-08-17 22:39
 * @Description:服务端通信
 */
@ServerEndpoint(value = "/websocket")
@Component
public class MyWebSocket {
    //打印日志信息
    private static final Logger logger = LoggerFactory.getLogger(MyWebSocket.class);

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    //与某个客户端的连接会话，需要通过它来给指定的客户端发送数据
    private Session session;

    //用以记录用户和房间号的对应关系(sessionId,room)
    private static HashMap<String,String> RoomForUser = new HashMap<String,String>();

    //用以记录房间和其中用户群的对应关系(room,List<用户>)
    public static HashMap<String, CopyOnWriteArraySet<User>> UserForRoom = new HashMap<String,CopyOnWriteArraySet<User>>();

    //用以记录房间和其中用户群的对应关系(room,List<用户>)
    public static HashMap<String,String> PwdForRoom = new HashMap<String,String>();

    //用来存放必应壁纸
    public static List<String> BingImages = new ArrayList<>();

    private Gson gson = new Gson();

    private Random random = new Random();

    /**
     * 连接建立成功调用的方法
     * @param session
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        logger.debug("---------------------成功与{}建立连接---------------------",session.getId());
        //将局部的session给成员session
        this.session = session;
        //统计在线人数
        addOnlineCount();
        Map<String, String> result = new HashMap<String, String> ();
        //存储缓存壁纸信息
        result.put("type", "bing");
        result.put("msg",BingImages.get(random.nextInt(BingImages.size())));
        //将当前用户上线消息推送给同房间的用户
        result.put("sendUser", "系统消息");
        result.put("id", session.getId());
        this.sendMessage(gson.toJson((result)));
    }

    /**
     * 关闭连接之后调用的方法
     */
    public void onclose() {

    }

    /**
     * 服务端收到客户端消息后调用的方法
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) {

    }

    /**
     * 连接发生错误时的调用方法
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {

    }


    /**
     * 在线人数统计
     * @Tips:保证同步
     */
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        MyWebSocket.onlineCount++;
    }

    //同房间用户下线, count--
    public static synchronized void subOnlineCount() {
        MyWebSocket.onlineCount--;
    }

    /**
     * 上线消息广播
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        //获取session来发送消息
        this.session.getBasicRemote().sendText(message);
    }
}
