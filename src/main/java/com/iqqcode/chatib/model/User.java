package com.iqqcode.chatib.model;

import com.iqqcode.chatib.web.MyWebSocket;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Author: iqqcode
 * @Date: 2020-08-18 07:21
 * @Description:用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class User {

    //room-id
    public String id;

    //用户名
    public String nickname;

    //room-聊天室
    public MyWebSocket webSocket;
}
