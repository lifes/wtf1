package com.hikvision.syncbd;

import com.hikvision.syncbd.common.Config;
import com.hikvision.syncbd.common.FileUtil;
import com.hikvision.syncbd.task.T4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.hikvision.syncbd.task.T1;
import com.hikvision.syncbd.task.T2;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author chenhuaming 2016-1-11
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String args[]) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                "spring.xml");

        logger.info("初始化成功----\n----\n----\n----\n----\n");
       // Thread t1 = new Thread(new T1(ctx));
       //Thread t2 = new Thread(new T2(ctx));
        //t1.start();
       // t2.start();

        wtf(ctx);
/*
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        logger.info("系统非正常退出");//unreachable
        System.exit(0);
    }

    public static void wtf(ApplicationContext ctx) {

        Config config = ctx.getBean(Config.class);
        System.out.println("您配置的文件路径是:" + config.getDirectoryPath());
        for (; ; ) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            	logger.error(e.getMessage(),e);
            }
            try {
                List<Thread> ts = new ArrayList<Thread>();
                File[] files = FileUtil.getFilesOnDirectoryNotIncludeFolder(config.getDirectoryPath());
                System.out.println("本地共读到" + files.length + "张图片");
                if (files.length > 0) {
                    int total = files.length;
                    int base = 10; //线程数
                    int size = (total - total % base) / base;
                    if (size > 0) {
                        for (int i = 0; i < base; i++) {
                            File[] temp = Arrays.copyOfRange(files, size * i, size * (i + 1) );
                            Thread t = new Thread(new T4(ctx, temp));
                            t.start();
                            ts.add(t);
                        }
                    }
                    if (total % base != 0) {
                        File[] temp = Arrays.copyOfRange(files, total - total % base, total);
                        Thread t = new Thread(new T4(ctx, temp));
                        t.start();
                        ts.add(t);
                    }
                }
                for (Thread t : ts) {
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                    	logger.error(e.getMessage(),e);
                    }
                }
            } catch (Throwable e) {
                logger.error(e.getMessage(),e);
            }
        }
    }
}
