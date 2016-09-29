package com.emagroup.sdk;

import com.anysdk.framework.java.AnySDKUser;

/**
 * Created by Administrator on 2016/9/7.
 */
public class EmaUser {

    private static EmaUser instance;


    //只能暂时委屈放在这里啦
    private static String appkey;

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        EmaUser.appkey = appkey;
    }



    private static String token;
    private boolean isLogin;

    private EmaUser(){}
    public static EmaUser getInstance(){
        if (instance == null) {
            instance = new EmaUser();
        }
        return instance;
    }


    public String getNickName(){
        return "";
    }


    public String getmUid(){
        return AnySDKUser.getInstance().getUserID();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setIsLogin(boolean isLogin) {
        this.isLogin = isLogin;
    }
    public boolean isLogin(){
        return isLogin;
    }
}
