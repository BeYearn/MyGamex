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
    private static String mUid;    // 官方平台uid
    private static String mAlienceUid;  // 渠道的uid
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
        return mAlienceUid;
    }

    public void setAllianceUid(String aUid){
        this.mAlienceUid=aUid;
    }

    public void setmUid(String uid){
        this.mUid=uid;
    }
    public String getmUid(){
        return mUid;
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
    public boolean getIsLogin(){
        return isLogin;
    }


    /**
     * 退出登录后，清空所有用户信息
     */
    public void clearUserInfo() {
        instance=null;  //这样再getInstance就得到的是一个空的实例 妙
    }

}
