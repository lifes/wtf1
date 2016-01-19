import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.hikvision.syncbd.common.FileUtil;
import com.hikvision.syncbd.model.CrossingInfo;
import com.hikvision.syncbd.model.LaneInfo;
import com.hikvision.syncbd.model.VehiclePass;
import com.hikvision.syncbd.model.VehicleViolation;


/**
 * @author chenhuaming
 *         2016-1-13
 */
public class Test {
    public static void main(String args[]) {
    	int[] imgs = new int[10001];
    	for(int i=0;i<imgs.length;i++){
    		imgs[i] =i;
    	}
    	int total = imgs.length;
    	if(total<1000){
			System.out.println("ok1");
		}else{
			int d =(total-total%1000)/1000;
			for(int i=0;i<d;i++){
				int[] temp = Arrays.copyOfRange(imgs, 1000*i, i*1000+1000);
				System.out.println(Arrays.toString(temp));
			}
			int[] temp = Arrays.copyOfRange(imgs, total-total%1000,total);
			System.out.println(Arrays.toString(temp));
		}	
    }

}