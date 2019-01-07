package com.box.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Curry on 2018/10/23.
 */

public class TimeUtil {

    public static String getTimeStr(){
        try {
            SimpleDateFormat format0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return format0.format(new Date().getTime());//这个就是把时间戳经过处理得到期望格式的时间
        }catch (Exception e){
            e.printStackTrace();
        }
        return "timenull";
    }

    public static String getTimeStr2(){
        try {
            SimpleDateFormat format0 = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
            return format0.format(new Date().getTime());//这个就是把时间戳经过处理得到期望格式的时间
        }catch (Exception e){
            e.printStackTrace();
        }
        return "timenull";
    }
}
