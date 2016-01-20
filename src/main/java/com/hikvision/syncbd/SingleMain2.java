package com.hikvision.syncbd;

import java.io.File;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.hikvision.syncbd.common.Config;
import com.hikvision.syncbd.common.FileUtil;
import com.hikvision.syncbd.service.SimpleService;

/**
 * @author chenhuaming 2016-1-19
 * 
 */
public class SingleMain2 {
	private final static Logger logger = LoggerFactory.getLogger(SingleMain2.class);
	public static void main(String args[]) {	
	  	logger.info("单线程！！！！！！！！！！!");
	  	logger.info("单线程！！！！！！！！！！!");
	  	logger.info("单线程！！！！！！！！！！!");
	  	logger.info("单线程！！！！！！！！！！!");
	  	logger.info("单线程！！！！！！！！！！!");
	  	logger.info("单线程！！！！！！！！！！!");   	
		ApplicationContext ctx = new ClassPathXmlApplicationContext("spring.xml");
		Config config = ctx.getBean(Config.class);
		SimpleService simpleService = ctx.getBean(SimpleService.class);
		for (;;) {
			try {
				File[] imgs = FileUtil
						.getFilesOnDirectoryNotIncludeFolder(config
								.getDirectoryPath());
				System.out.println("本地共读到" + imgs.length + "张图片");
				int base = 100;
				if (imgs.length < base) {
					simpleService.uploadBatch(imgs, config);
				} else {
					int total = imgs.length;
					int d = (total - total % base) / base;
					for (int i = 0; i < d; i++) {
						File[] temp = Arrays.copyOfRange(imgs, base * i, i
								* base + base);
						simpleService.uploadBatch(temp, config);
					}
					if (total % base != 0) {
						File[] temp = Arrays.copyOfRange(imgs, total - total
								% base, total);
						simpleService.uploadBatch(temp, config);
					}
				}
			} catch (Throwable e) {
				logger.error(e.getMessage(),e);
			}
		}
	}
}
