package com.hikvision.syncbd.task;

import java.io.File;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import com.hikvision.syncbd.common.Config;
import com.hikvision.syncbd.service.SimpleService;

/**
 * @author chenhuaming 2016-1-15
 * 
 */
public class T4 implements Runnable {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	@SuppressWarnings("unused")
	private ApplicationContext ctx;
	private Config config;
	private SimpleService simpleService;
	private File[] imgs;

	public T4(ApplicationContext ctx, File[] imgs) {
		this.ctx = ctx;
		this.config = ctx.getBean(Config.class);
		this.simpleService = ctx.getBean(SimpleService.class);
		this.imgs = imgs;
	}

	public void run() {
		try {
			int base = 100;
			if (imgs.length < base) {
				simpleService.uploadBatch(imgs, config);
			} else {
				int total = imgs.length;
				int d = (total - total % base) / base;
				for (int i = 0; i < d; i++) {
					File[] temp = Arrays.copyOfRange(imgs, base * i, i * base
							+ base);
					simpleService.uploadBatch(temp, config);
				}
				if (total % base != 0) {
					File[] temp = Arrays.copyOfRange(imgs,
							total - total % base, total);
					simpleService.uploadBatch(temp, config);
				}
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}
}
