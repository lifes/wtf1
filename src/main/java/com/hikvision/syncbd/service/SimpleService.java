package com.hikvision.syncbd.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONArray;
import com.hikvision.syncbd.common.Config;
import com.hikvision.syncbd.common.FileUtil;
import com.hikvision.syncbd.common.HttpHelper;
import com.hikvision.syncbd.common.WrapperException;
import com.hikvision.syncbd.mapper.SimpleMapper;
import com.hikvision.syncbd.model.CrossingInfo;
import com.hikvision.syncbd.model.LaneInfo;
import com.hikvision.syncbd.model.VehiclePass;
import com.hikvision.syncbd.model.VehicleViolation;

/**
 * @author chenhuaming 2016-1-11
 * 
 */
@Service
public class SimpleService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private SimpleMapper simpleMapper;

	public void uploadCrossingInfo(Config config) {
		List<CrossingInfo> all = simpleMapper.getAllCrossingInfo();
		logger.info("共查到" + all.size() + "条CI数据");
		int base = 0;
		for (;;) {
			int total = simpleMapper.getTotalNumber("BMS_CROSSING_INFO");
			if (total > base) {
				logger.info("新增" + (total - base) + "条CI数据");
				all = simpleMapper.getAllCrossingInfo();
				List<CrossingInfo> xz = all.subList(base, total);
				upload(xz, "BMS_CROSSING_INFO", config);
			}
			base = total;
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}
	}

	public void uploadLaneInfo(Config config) {
		List<LaneInfo> all = simpleMapper.getAllLaneInfo();
		logger.info("共查到" + all.size() + "条LI数据");
		int base = 0;
		for (;;) {
			int total = simpleMapper.getTotalNumber("BMS_LANE_INFO");
			if (total > base) {
				logger.info("新增" + (total - base) + "条LI数据");
				all = simpleMapper.getAllLaneInfo();
				List<LaneInfo> xz = all.subList(base, total);
				upload(xz, "BMS_LANE_INFO", config);
			}
			base = total;
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}
	}

	private <T> void upload(List<T> all, String tableName, Config config) {
		int u = 0;
		while (u < all.size()) {
			JSONArray records = new JSONArray();
			for (int i = 0; i < 50; i++) {
				if (i < 50 && u < all.size()) {
					records.add(all.get(u));
				} else {
					break;
				}
				u++;
			}
			try {
				if (records.size() > 0) {
					HttpHelper.uploadRecordsByTable(tableName, records, config,
							null);

					logger.info("Success:上传" + records.size() + "条数据成功");
				}
			} catch (Throwable e) {
				logger.error("Error:上传" + records.size() + "条数据失败");
			}
		}
	}

	public void uploadOne(File img, Config config) {
		logger.info("===============uploadOne begin");
		String url = null;
		try {
			String imgName = img.getName();
			if (!imgName.contains("A")) {
				return;
			}
			String[] args = imgName.split("A");
			if (args.length < 2) {
				return;
			}
			if (!(args[1].contains("1B") || args[1].contains("2B"))) {
				return;
			}
			long beginTime = System.currentTimeMillis();
			url = HttpHelper.uploadImgSingleAndGetTheUrlBack(img, config);
			long endTime = System.currentTimeMillis();
			logger.info("上传一张图片需要" + (endTime - beginTime) / 1000.0 + "秒钟");
			logger.info("Success:图片上传成功1");
		} catch (Throwable e) {
			logger.error("Error:上传图片失败1");
		}
		if (url != null) {
			String imgName = img.getName();
			String[] args = imgName.split("A");
			String index = args[0];
			String flag = args[1];
			// 更新CrossingPass
			logger.info("1B---");
			if (flag.contains("1B")) {
				VehiclePass vehiclePass = null;
				try {
					simpleMapper.updateVehiclePass(url, index);
					vehiclePass = simpleMapper.getVehiclePassByIndex(index);
					logger.info("Success:更新本地数据库成功2");
				} catch (Throwable e) {
					logger.error("Error:更新本地数据库失败2");
				}
				if (vehiclePass != null) {
					JSONArray records = new JSONArray();
					records.add(vehiclePass);
					try {
						HttpHelper.uploadRecordsByTable("BMS_VEHICLE_PASS",
								records, config, null);
						logger.info("Success:上传一条数据到云端成功3");
					} catch (Throwable e) {
						logger.error("Error:上传一条数据到云端失败3");
					}
				}
			}
			logger.info("2B---");
			if (flag.contains("2B")) {
				VehicleViolation vehicleViolation = null;
				try {
					simpleMapper.updateVehicleViolation(url, index);
					vehicleViolation = simpleMapper
							.getVehicleViolationByIndex(index);
					logger.info("Success:更新本地数据库成功2");
				} catch (Throwable e) {
					logger.error("Error:更新本地数据库失败2");
				}
				if (vehicleViolation != null) {
					JSONArray records = new JSONArray();
					records.add(vehicleViolation);
					try {
						HttpHelper.uploadRecordsByTable(
								"BMS_VEHICLE_VIOLATION", records, config, null);
						logger.info("Success:上传一条数据到云端成功3");
					} catch (Throwable e) {
						logger.error("Error:上传一条数据到云端失败3");
					}
				}
			}
		}
		FileUtil.del(img);
		logger.info("===============uploadOne end");
	}

	// 每次上传1000张
	public void uploadBatch(File[] imgs, Config config) {
		List<File> okImgs = new ArrayList<File>();
		List<VehiclePass> okVehiclePasses = new ArrayList<VehiclePass>();
		List<VehicleViolation> okVehicleViolations = new ArrayList<VehicleViolation>();
		long beginTime = System.currentTimeMillis();
		for (File img : imgs) {
			String imgName = img.getName();
			logger.info("imgName===:" + imgName);
			String flag = "";
			String index = "";
			String url = null;
			if (imgName.contains("A")) {
				String[] args = imgName.split("A");
				if (args.length >= 2) {
					index = args[0].trim();
					flag = args[1];
					if ((args[1].contains("1B") || args[1].contains("2B"))) {
						try {
							url = HttpHelper.uploadImgSingleAndGetTheUrlBack(
									img, config);
							logger.info("Success:图片上传成功1");
						} catch (Throwable e) {
							logger.error("error:图片上传失败", e);
						}
					}
				}
			}
			if (url != null) {
				try {
					// 更新CrossingPass
					logger.info("1B---");
					if (flag.contains("1B")) {
						VehiclePass vehiclePass = null;
						simpleMapper.updateVehiclePass(url, index);
						vehiclePass = simpleMapper.getVehiclePassByIndex(index);
						logger.info("Success:更新本地数据库成功2");
						if (vehiclePass != null) {
							logger.info("查询到一条数据v1");
							okVehiclePasses.add(vehiclePass);
						} else {
							logger.info("未查询到数据v1");
						}
					}
					logger.info("2B---");
					if (flag.contains("2B")) {
						VehicleViolation vehicleViolation = null;
						simpleMapper.updateVehicleViolation(url, index);
						vehicleViolation = simpleMapper
								.getVehicleViolationByIndex(index);
						logger.info("Success:更新本地数据库成功2");
						if (vehicleViolation != null) {
							logger.info("查询到一条数据v2");
							okVehicleViolations.add(vehicleViolation);
						} else {
							logger.info("未查询到数据v2");
						}
					}
				} catch (Throwable e) {
					logger.error("查到重复数据！！！！",e);
				}
			}
			okImgs.add(img);
		}
		// 先删除保证不会出现重复数据
		FileUtil.del(okImgs);
		JSONArray records1 = new JSONArray();
		for (VehiclePass r : okVehiclePasses) {
			records1.add(r);
		}
		JSONArray records2 = new JSONArray();
		for (VehicleViolation r : okVehicleViolations) {
			records2.add(r);
		}
		if (records1.size() > 0) {
			try {
				HttpHelper.uploadRecordsByTable("BMS_VEHICLE_PASS", records1,
						config, null);
				logger.info("上传" + records1.size() + "条数据成功");
			} catch (Exception e) {
				logger.error("上传" + records1.size() + "条数据失败", e);
			}
		}
		if (records2.size() > 0) {
			try {
				HttpHelper.uploadRecordsByTable("BMS_VEHICLE_VIOLATION",
						records2, config, null);
				logger.info("上传" + records2.size() + "条数据成功");
			} catch (Exception e) {
				logger.error("上传" + records2.size() + "条数据失败", e);
			}
		}
		long endTime = System.currentTimeMillis();
		logger.info("上传" + okImgs.size() + "张图片成功，需要" + (endTime - beginTime)
				/ 1000.0 + "秒钟");
	}
}
