package com.emagroup.sdk;

/**
 * Created by Administrator on 2016/8/16.
 */
public class Instants {

    private static String serverUrl="https://testing-platform.lemonade-game.com:8443";
    //private static String serverUrl="https://platform.lemonade-game.com";

    public static final String CREAT_WEAKCOUNT_URL = serverUrl+"/ema-platform/member/createAlianceAccount";

    public static final String UPDATE_WEAKCOUT_URL = serverUrl+"/ema-platform/member/updateAlianceAccount";

    public static final String HEART_BEAT_URL = serverUrl+"/ema-platform/member/heartbeat";

    //创建订单接口
    public static final String CREAT_ORDER_URL = serverUrl+"/ema-platform/order/createOrder";

    public static final String SDK_STATUS_URL= serverUrl+"/ema-platform/admin/getSystemInfo";

    public static final String GET_KEY_INFO=serverUrl+"/ema-platform/admin/channelKeyInfo";
}
