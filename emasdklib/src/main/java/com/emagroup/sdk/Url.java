package com.emagroup.sdk;

/**
 * Created by Administrator on 2016/8/16.
 */
public class Url {

    public static final String TESTING_SERVER_URL="https://testing-platform.lemonade-game.com:8443";
    public static final String STAGING_SERVER_URL="https://staging-platform.lemonade-game.com";
    public static final String PRODUCTION_SERVER_URL="https://platform.lemonade-game.com";


    private static String serverUrl="https://platform.lemonade-game.com";

    public static void setServerUrl(String url){
        serverUrl=url;
    }

    public static String createAlianceAccount(){
        return serverUrl+"/ema-platform/member/createAlianceAccount";
    }

    public static String updateAlianceAccount(){
        return serverUrl+"/ema-platform/member/updateAlianceAccount";
    }

    public static String heartbeat(){
      //  return serverUrl+"/ema-platform/member/heartbeat";
        return serverUrl+"/ema-platform/member/newheartbeat";
    }

    public static String createOrder(){
        return serverUrl+"/ema-platform/order/createOrder";
    }

    public static String rejectOrder(){
        return serverUrl+"/ema-platform/order/rejectOrder";
    }

    public static String getSystemInfo(){
       // return serverUrl+"/ema-platform/admin/getSystemInfo";
        return serverUrl+"/ema-platform/admin/getSystemInfoEx";
    }

    public static String channelKeyInfo(){
        return serverUrl+"/ema-platform/admin/channelKeyInfo";
    }




    //-----------------------------------UC 特有接口---------------------------------------------------
    public static String getUCAccontInfo(){
        return serverUrl+"/ema-platform/channelLogin/uc";
    }
    //-----------------------------------UC 特有接口---------------------------------------------------

}
