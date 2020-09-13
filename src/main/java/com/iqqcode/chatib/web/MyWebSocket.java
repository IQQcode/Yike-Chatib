package com.iqqcode.chatib.web;

import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iqqcode.chatib.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Author: iqqcode
 * @Date: 2020-08-17 22:39
 * @Description:服务端通信
 */
@Component
@ServerEndpoint(value = "/websocket")
public class MyWebSocket {
    //打印日志信息
    private static final Logger logger = LoggerFactory.getLogger(MyWebSocket.class);

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    //与某个客户端的连接会话，需要通过它来给指定的客户端发送数据
    private Session session;

    //用以记录用户和房间号的对应关系(sessionId,room)
    private static Map<String, String> RoomForUser = new ConcurrentHashMap<String, String>();

    //用以记录房间和其中用户群的对应关系(room,List<用户>)
    public static Map<String, CopyOnWriteArraySet<User>> UserForRoom = new ConcurrentHashMap<String, CopyOnWriteArraySet<User>>();

    //用以记录房间和其中用户群的对应关系(room,List<用户>)
    public static Map<String, String> PwdForRoom = new ConcurrentHashMap <String, String>();

    //用来存放必应壁纸
    public static List<String> BingImages = new ArrayList<>();

    private Gson gson = new Gson();

    private Random random = new Random();

    /**
     * 连接建立成功调用的方法
     *
     * @param session
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        logger.debug("---------------------成功与{}建立连接---------------------", session.getId());
        //将局部的session给成员session
        this.session = session;
        //统计在线人数
        addOnlineCount();
        Map<String, String> result = new HashMap<String, String>();
        //存储缓存壁纸信息
        result.put("type", "bing");
        result.put("msg", BingImages.get(random.nextInt(BingImages.size())));
        //将当前用户上线消息推送给同房间的用户
        result.put("sendUser", "系统消息");
        result.put("id", session.getId());
        this.sendMessage(gson.toJson((result)));
    }

    /**
     * 服务端收到客户端消息后调用的方法
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        //将json-message转为message对象
        Map<String, String> map = new Gson().fromJson(message, new TypeToken<HashMap<String, String>>(){}.getType());
        //容器来保存转换后的数据
        Map<String, String> result = new HashMap<>();
        User user = null;
        String shiels = map.containsKey("shiels") ? map.get("shiels").toString() : null;
        switch (map.get("type")) {
            //消息数据
            case "msg":
                //获取要发送的用户
                user = getUser(session);
                //将数据放入容器
                result.put("type", "msg"); //消息类型
                result.put("msg", map.get("msg")); //消息内容
                result.put("sendUser", user.getNickname()); //发送给谁
                result.put("shake", map.get("shake")); //消息动画
                break;
            //Room数据
            case "init":
                //获取当前Room信息: room-id,username,room-password
                String room = map.get("room");
                String nick = map.get("nick");
                String pwd = map.get("pwd");
                if (room != null && nick != null) {
                    user = new User(session.getId(), nick, this);
                    //如果房间不存在，新建房间
                    if (UserForRoom.get(room) == null) {
                        CopyOnWriteArraySet<User> roomUsers = new CopyOnWriteArraySet<>();
                        roomUsers.add(user);
                        UserForRoom.put(room, roomUsers);
                        if (StrUtil.isNotEmpty(pwd)) {
                            PwdForRoom.put(room, pwd);
                        }
                        RoomForUser.put(session.getId(), room);
                    } else {
                        UserForRoom.get(room).add(user);
                        RoomForUser.put(session.getId(), room);
                    }
                    result.put("type", "init");
                    result.put("msg", nick + "成功加入房间");
                    result.put("sendUser", "系统消息");
                }
                break;
            //背景图片
            case "img":
                user = getUser(session);
                result.put("type", "img");
                result.put("msg", map.get("msg"));
                result.put("sendUser", user.getNickname());
                break;
            case "ping":
                return;
        }
        //StrUtil处理表情包字符串
        if (StrUtil.isEmpty(shiels)) {
            sendMessagesOther(getUsers(session), gson.toJson(result));
        } else {
            sendMessagesOther(getUsers(session), gson.toJson(result), shiels);
        }
    }

    /**
     * 关闭连接之后调用的方法
     * 从容器中删除指定用户
     */
    @OnClose
    public void onClose() {
        subOnlineCount();
        //从session中获取到该用户
        CopyOnWriteArraySet<User> users = getUsers(session);
        if (users!=null){
            String nick = "某人";
            for (User user : users) {
                if (user.getId().equals(session.getId())){
                    nick = user.getNickname();
                }
            }
            //系统消息广播
            Map<String,String> result = new HashMap<>();
            result.put("type","init");
            result.put("msg",nick+"离开房间");
            result.put("sendUser","系统消息");
            sendMessagesOther(users,gson.toJson(result));
            //users用户列表中删除该用户
            User closeUser = getUser(session);
            users.remove(closeUser);
            //逻辑判断,如果当前Room无用户，则自动关闭
            if (users.size() == 0){
                String room = RoomForUser.get(session.getId());
                UserForRoom.remove(room);
                PwdForRoom.remove(room);
            }
            RoomForUser.remove(session.getId());
        }
    }

    /**
     * 连接发生错误时的调用方法
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.debug("---------------------与{}的连接发生错误---------------------", session.getId());
        subOnlineCount();
        CopyOnWriteArraySet<User> users = getUsers(session);
        if (users != null) {
            String nick = "某人";
            for (User user : users) {
                if (user.getId().equals(session.getId())) {
                    nick = user.getNickname();
                }
            }
            Map<String, String> result = new HashMap<>();
            result.put("type", "init");
            result.put("msg", nick + "离开房间");
            result.put("sendUser", "系统消息");
            sendMessagesOther(users, gson.toJson(result));
            User closeUser = getUser(session);
            users.remove(closeUser);
            if (users.size() == 0) {
                String room = RoomForUser.get(session.getId());
                UserForRoom.remove(room);
                PwdForRoom.remove(room);
            }
            RoomForUser.remove(session.getId());
        }
        error.printStackTrace();
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
     * 文本消息发送
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        //获取session来发送消息
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 通过当前用户的session获取待发送的目标用户
     * @param session
     * @return
     */
    private User getUser(Session session) {
        String room = RoomForUser.get(session.getId());
        CopyOnWriteArraySet<User> users = UserForRoom.get(room);
        for (User user : users) {
            if (session.getId().equals(user.getId())) {
                return user;
            }
        }
        return null;
    }

    /**
     * 通过当前用户的session获取当前room内的所有用户,存入Set
     * @param session
     * @return
     */
    private CopyOnWriteArraySet<User> getUsers(Session session) {
        String room = RoomForUser.get(session.getId());
        CopyOnWriteArraySet<User> users = UserForRoom.get(room);
        return users;
    }


    /**
     * 给room内用户群发信息(除自己外)
     * @param users 用户集合
     * @param message 消息数据(无表情)
     */
    private void sendMessagesOther(CopyOnWriteArraySet<User> users, String message) {
        //遍历Set中的每个用户，群发
        for (User item : users) {
            //不能给自己发
            if (item.getWebSocket() != this) {
                try {
                    item.getWebSocket().sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 给room内所有用户群发信息(除自己外)
     * @param users 用户集合
     * @param message 消息数据
     * @param shiel 表情字符串
     */
    private void sendMessagesOther(CopyOnWriteArraySet<User> users, String message, String shiel) {
        //将存表情包字符串的数组转为List
        //var shiels = new Array();
        List<String> shiels = Arrays.asList(shiel.split(","));
        //群发消息
        for (User item : users) {
            if (item.getWebSocket() != this && !shiels.contains(item.getId())){
                try {
                    item.getWebSocket().sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 给房间的所有人发送消息
     * @param users
     * @param message
     */
    private void sendMessagesAll(CopyOnWriteArraySet<User> users, String message){
        //群发消息
        for (User item : users) {
            try {
                item.getWebSocket().sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
