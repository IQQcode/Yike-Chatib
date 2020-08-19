package com.iqqcode.chatib.config;

import com.iqqcode.chatib.util.BingImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @Author: iqqcode
 * @Date: 2020-08-19 17:12
 * @Description:该类用于项目启动时下载Bing壁纸
 */
@Component
public class DownloadBingRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadBingRunner.class);

    @Override
    public void run(String... args) throws Exception {
        Integer sum = BingImageUtil.download(0,7);
        sum += BingImageUtil.download(7,7);
        LOGGER.debug("本次同步了"+sum+"张壁纸！");
    }
}
