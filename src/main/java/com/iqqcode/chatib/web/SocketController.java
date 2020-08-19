package com.iqqcode.chatib.web;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.iqqcode.chatib.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Author: iqqcode
 * @Date: 2020-08-19 09:46
 * @Description:
 */

@RestController
@RequestMapping("/ws")
public class SocketController {

    private static final Logger logger = LoggerFactory.getLogger(SocketController.class);

    //图片保存路径
    private String imgPath = new ApplicationHome(getClass()).getSource().getParentFile().toString()+"/img/";


    public static Map<Long,String> img = new HashMap();

    /**
     * 根据房间号获得其中的用户
     * @param room 房间号
     * @return
     */
    @RequestMapping("/online")
    public Map<String,Object> online(String room){
        Map<String,Object> result = new HashMap<>();
        CopyOnWriteArraySet<User> rooms = MyWebSocket.UserForRoom.get(room);
//        List<String> nicks = new ArrayList<>();
        List<Map<String,String>> users = new ArrayList<>();
        if (rooms != null){
            rooms.forEach(user -> {
                Map<String,String> map = new HashMap<>();
                map.put("nick",user.getNickname());
                map.put("id",user.getId());
                users.add(map);
            });
            result.put("onlineNum",rooms.size());
            result.put("onlineUsera",users);
        }else {
            result.put("onlineNum",0);
            result.put("onlineUsera",null);
        }
        return result;
    }

    /**
     * 判断昵称在某个房间中是否已存在，房间是否有密码，如果有，用户输入的密码又是否正确
     * @param room 房间号
     * @param nick 昵称
     * @param pwd 密码
     * @return
     */
    @RequestMapping("/judgeNick")
    public Map<String,Object> judgeNick(String room, String nick, String pwd){
        Map<String,Object> result = new HashMap<>();
        result.put("code",0);
        CopyOnWriteArraySet<User> rooms = MyWebSocket.UserForRoom.get(room);
        if (rooms != null){
            rooms.forEach(user -> {
                if (user.getNickname().equals(nick)){
                    result.put("code",1);
                    result.put("msg","昵称已存在，请重新输入");
                    logger.debug("有重复");
                }
            });
            if ((Integer)result.get("code") != 0){
                return result;
            }
            String password = MyWebSocket.PwdForRoom.get(room);
            if (StrUtil.isNotEmpty(password) && !(pwd.equals(password))){
                result.put("code",2);
                result.put("msg","密码错误，请重新输入");
                return result;
            }else {
                result.put("code",3);
                result.put("msg","房间无密码");
                return result;
            }
        }
        return result;
    }


    /**
     * 实现文件上传
     * @param request
     * @param file
     * @return
     */
    @RequestMapping("/fileUpload")
    public Map<String,Object> fileUpload(HttpServletRequest request, @RequestParam("file") MultipartFile file){
        Map<String,Object> result = new HashMap<>();
        //获取项目访问路径
        String root = request.getRequestURL().toString().replace(request.getRequestURI(),"");
        if(file.isEmpty()){
            return null;
        }
        //获取文件名
        String fileName = file.getOriginalFilename();
        //重命名文件
        String imgName = RandomUtil.randomUUID() + fileName.substring(fileName.lastIndexOf("."));
        logger.debug("上传图片保存在：" + imgPath + imgName);
        File dest = new File(imgPath + imgName);
        img.put(System.currentTimeMillis(),imgPath + imgName);
        //判断文件父目录是否存在
        if(!dest.getParentFile().exists()){
            dest.getParentFile().mkdir();
        }
        try {
            //保存文件
            file.transferTo(dest);
            //返回图片访问路径
            result.put("url",root +"/img/" + imgName);
            logger.debug("图片保存成功，访问路径为："+result.get("url"));
            return result;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            logger.error("图片保存失败！");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("图片保存失败！");
        }
        return null;
    }

    /**
     * 获取所有房间
     * @return
     */
    @RequestMapping("/allRoom")
    public Map<String,Object> allRoom(){
        Map<String,Object> result = new HashMap<>();
        HashMap<String,CopyOnWriteArraySet<User>> userForRoom = MyWebSocket.UserForRoom;
        List<String> rooms = new ArrayList<>();
        for (String key : userForRoom.keySet()) {
            rooms.add(key);
        }
        result.put("rooms",rooms);
        return result;
    }

}
