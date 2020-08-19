package com.iqqcode.chatib.config;

import com.iqqcode.chatib.util.BingImageUtil;
import com.iqqcode.chatib.web.SocketController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author: iqqcode
 * @Date: 2020-08-19 17:13
 * @Description:此类为一个定时任务，使用前须在启动类上添加@EnableScheduling注解
 */

// 标注一个类为Spring容器的Bean
@Component
public class QuartzService {

    private static final Logger logger = LoggerFactory.getLogger(QuartzService.class);

    /**
     * 定期删除聊天图片
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void timerToNow(){
        logger.debug("开始查询需要删除的图片。。。。。。。。。。");
        Map<Long,String> img = SocketController.img;
        Long now = System.currentTimeMillis()-60000;
        Iterator<Map.Entry<Long, String>> it = img.entrySet().iterator();
        int a = 0;
        while(it.hasNext()){
            Map.Entry<Long, String> entry = it.next();
            if (entry.getKey() < now){
                if (deleteFile(entry.getValue())){
                    it.remove();
                    a++;
                }
            }
        }
        logger.debug("删除任务执行完毕，共删除"+a+"张图片");
    }

    /**
     * 定期下载必应壁纸,每天中午12点触发
     */
    @Scheduled(cron = "0 0 12 * * ?")
    public void dowBingImage(){
        logger.debug("开始同步必应壁纸。。。。。。。。。。");
        Integer i = BingImageUtil.download(0,1);
        logger.debug("本次同步了"+i+"张壁纸！");
    }

    private boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                logger.debug("图片"+fileName+"删除成功");
                return true;
            } else {
                logger.debug("图片"+fileName+"删除成功");
                return false;
            }
        } else {
            logger.debug("图片"+fileName+"删除失败");
            return false;
        }
    }

}

