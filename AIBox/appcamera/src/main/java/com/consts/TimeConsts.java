package com.consts;

/**
 * @author zhangh
 * @version 1.0.1
 */

public class TimeConsts {
    //收到服务器消息的时间
    public static long OPEN_MESS_RECEIVED_TIME;
    //数据开始收集
    public static long OPEN_COLLECTION_START_TIME;
    //数据结束收集
    public static long OPEN_COLLECTION_END_TIME;
    //下载图片开始的时间
    public static long OPEN_DOWNLOAD_IMAGES_START_TIME;
    //下载图片完成的时间
    public static long OPEN_DOWNLOAD_IMAGES_END_TIME;
    //获取重量开始的时间
    public static long OPEN_GET_WEIGTHS_START_TIME;
    //获取重量开始的时间
    public static long OPEN_GET_WEIGTHS_END_TIME;
    //整理参数开始的时间
    public static long OPEN_DISPOSAL_DATA_START_TIME;
    //整理参数结束的时间
    public static long OPEN_DISPOSAL_DATA_END_TIME;
    //上传参数开始的时间
    public static long OPEN_UPLOAD_DATA_START_TIME;
    //上传参数返回的时间
    public static long OPEN_UPLOAD_DATA_END_TIME;


    //收到服务器消息的时间
    public static long CLOSE_MESS_RECEIVED_TIME;
    //数据开始收集
    public static long CLOSE_COLLECTION_START_TIME;
    //数据结束收集
    public static long CLOSE_COLLECTION_END_TIME;
    //下载图片开始的时间
    public static long CLOSE_DOWNLOAD_IMAGES_START_TIME;
    //下载图片完成的时间
    public static long CLOSE_DOWNLOAD_IMAGES_END_TIME;
    //获取重量开始的时间
    public static long CLOSE_GET_WEIGTHS_START_TIME;
    //获取重量开始的时间
    public static long CLOSE_GET_WEIGTHS_END_TIME;
    //整理参数开始的时间
    public static long CLOSE_DISPOSAL_DATA_START_TIME;
    //整理参数结束的时间
    public static long CLOSE_DISPOSAL_DATA_END_TIME;
    //上传参数开始的时间
    public static long CLOSE_UPLOAD_DATA_START_TIME;
    //上传参数返回的时间
    public static long CLOSE_UPLOAD_DATA_END_TIME;

    //订单开始的时间
    public static long ORDER_START_TIME;
    //订单完成的时间
    public static long ORDER_END_TIME;

    public static void init() {
        OPEN_MESS_RECEIVED_TIME = 0;
        OPEN_COLLECTION_START_TIME = 0;
        OPEN_COLLECTION_END_TIME = 0;
        OPEN_DOWNLOAD_IMAGES_START_TIME = 0;
        OPEN_DOWNLOAD_IMAGES_END_TIME = 0;
        OPEN_GET_WEIGTHS_START_TIME = 0;
        OPEN_GET_WEIGTHS_END_TIME = 0;
        OPEN_DISPOSAL_DATA_START_TIME = 0;
        OPEN_DISPOSAL_DATA_END_TIME = 0;
        OPEN_UPLOAD_DATA_START_TIME = 0;
        OPEN_UPLOAD_DATA_END_TIME = 0;


        CLOSE_MESS_RECEIVED_TIME = 0;
        CLOSE_COLLECTION_START_TIME = 0;
        CLOSE_COLLECTION_END_TIME = 0;
        CLOSE_DOWNLOAD_IMAGES_START_TIME = 0;
        CLOSE_DOWNLOAD_IMAGES_END_TIME = 0;
        CLOSE_GET_WEIGTHS_START_TIME = 0;
        CLOSE_GET_WEIGTHS_END_TIME = 0;
        CLOSE_DISPOSAL_DATA_START_TIME = 0;
        CLOSE_DISPOSAL_DATA_END_TIME = 0;
        CLOSE_UPLOAD_DATA_START_TIME = 0;
        CLOSE_UPLOAD_DATA_END_TIME = 0;

        ORDER_START_TIME = 0;
        ORDER_END_TIME = 0;
    }

    public static String write() {
        StringBuilder mess = new StringBuilder("订单时间记录\n");
        mess.append("开门数据\n");
        mess.append("接收到指令后触发时间:").append(OPEN_MESS_RECEIVED_TIME - OPEN_COLLECTION_START_TIME).append("ms\n");
        mess.append("下载图片时间:").append(OPEN_DOWNLOAD_IMAGES_END_TIME - OPEN_DOWNLOAD_IMAGES_START_TIME).append("ms\n");
        mess.append("下载重力数据时间:").append(OPEN_GET_WEIGTHS_END_TIME - OPEN_GET_WEIGTHS_START_TIME).append("ms\n");
        mess.append("下载数据总耗时时间:").append(OPEN_COLLECTION_END_TIME - OPEN_COLLECTION_START_TIME).append("ms\n");
        mess.append("整理数据时间:").append(OPEN_DISPOSAL_DATA_END_TIME - OPEN_DISPOSAL_DATA_START_TIME).append("ms\n");
        mess.append("上传参数时间:").append(OPEN_UPLOAD_DATA_END_TIME - OPEN_UPLOAD_DATA_START_TIME).append("ms\n");
        mess.append("关门数据\n");
        mess.append("接收到指令后触发时间:").append(CLOSE_MESS_RECEIVED_TIME - CLOSE_COLLECTION_START_TIME).append("ms\n");
        mess.append("下载图片时间:").append(CLOSE_DOWNLOAD_IMAGES_END_TIME - CLOSE_DOWNLOAD_IMAGES_START_TIME).append("ms\n");
        mess.append("下载重力数据时间:").append(CLOSE_GET_WEIGTHS_END_TIME - CLOSE_GET_WEIGTHS_START_TIME).append("ms\n");
        mess.append("下载数据总耗时时间:").append(CLOSE_COLLECTION_END_TIME - CLOSE_COLLECTION_START_TIME).append("ms\n");
        mess.append("整理数据时间:").append(CLOSE_DISPOSAL_DATA_END_TIME - CLOSE_DISPOSAL_DATA_START_TIME).append("ms\n");
        mess.append("上传参数时间:").append(CLOSE_UPLOAD_DATA_END_TIME - CLOSE_UPLOAD_DATA_START_TIME).append("ms\n");
        mess.append("订单完成耗时:").append(ORDER_END_TIME - ORDER_START_TIME).append("ms\n");
        init();
        return mess.toString();
    }
}
