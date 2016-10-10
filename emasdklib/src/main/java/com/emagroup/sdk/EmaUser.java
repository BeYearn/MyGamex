package com.emagroup.sdk;


import android.text.TextUtils;

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
    private static String mUid;
    private static String nickName;
    private boolean isLogin;

    private EmaUser(){}
    public static EmaUser getInstance(){
        if (instance == null) {
            instance = new EmaUser();
        }
        return instance;
    }


    public String getNickName(){
        if(TextUtils.isEmpty(nickName)){
            return "";
        }else {
            return nickName;
        }
    }

    public void setNickName(String nickName){
        this.nickName=nickName;
    }

    public String getAllianceUid(){
        return mUid;
    }

    public void setmUid(String uid){
        this.mUid=uid;
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
