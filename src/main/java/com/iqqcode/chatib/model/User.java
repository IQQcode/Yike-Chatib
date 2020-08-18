package com.iqqcode.chatib.model;

import com.iqqcode.chatib.web.MyWebSocket;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: iqqcode
 * @Date: 2020-08-18 07:21
 * @Description:用户实体类
 */

@Data
@EqualsAndHashCode
public class User {

    //房间号id
    public String id;

    //用户名
    public String nickname;

    //该用户对应的聊天室
    public MyWebSocket webSocket;
}
