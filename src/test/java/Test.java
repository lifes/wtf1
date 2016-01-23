import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.hikvision.syncbd.SingleMain2;
import com.hikvision.syncbd.common.Config;
import com.hikvision.syncbd.common.FileUtil;
import com.hikvision.syncbd.common.HttpHelper;
import com.hikvision.syncbd.mapper.SimpleMapper;
import com.hikvision.syncbd.model.CrossingInfo;
import com.hikvision.syncbd.model.LaneInfo;
import com.hikvision.syncbd.model.VehiclePass;
import com.hikvision.syncbd.model.VehicleViolation;


/**
 * @author chenhuaming
 *         2016-1-13
 */
public class Test {

	@Autowired
	private SimpleMapper simpleMapper;
	private final static Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String args[]) {
    
    }
    public void uploadBatch(File[] imgs, Config config) {
		List<File> okImgs = new ArrayList<File>();
		List<VehiclePass> okVehiclePasses = new ArrayList<VehiclePass>();
		List<VehicleViolation> okVehicleViolations = new ArrayList<VehicleViolation>();
		long beginTime = System.currentTimeMillis();
		for (File img : imgs) {
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
			String url = null;
			try {
				url = HttpHelper.uploadImgSingleAndGetTheUrlBack(img, config);
				logger.info("Success:图片上传成功1");
			} catch (Throwable e) {
				logger.error("error:图片上传失败", e);
			}			
			if (url != null) {
				String index = args[0].trim();
				String flag = args[1];
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
					}else{
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
					}else{
						logger.info("未查询到数据v2");
					}
				}
				okImgs.add(img);
			}
		}
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
				HttpHelper.uploadRecordsByTable("BMS_VEHICLE_PASS", records1, config, null);
				logger.info("上传"+records1.size()+"条数据成功");
			} catch (Exception e) {
				logger.error("上传"+records1.size()+"条数据失败",e);
			}
		}
		if (records2.size() > 0) {
			try {
				HttpHelper.uploadRecordsByTable("BMS_VEHICLE_VIOLATION", records2, config, null);
				logger.info("上传"+records2.size()+"条数据成功");
			} catch (Exception e) {
				logger.error("上传"+records2.size()+"条数据失败",e);
			}
		}
		long endTime = System.currentTimeMillis();
		logger.info("上传"+okImgs.size()+"张图片成功，需要" + (endTime - beginTime) / 1000.0 + "秒钟");
		FileUtil.del(okImgs);
	}

}