package com.emagroup.sdk;


import android.text.TextUtils;

import com.anysdk.framework.java.AnySDKParam;
import com.anysdk.framework.java.AnySDKUser;

import java.util.HashMap;
import java.util.Map;

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

    private EmaUser() {
    }

    public static EmaUser getInstance() {
        if (instance == null) {
            instance = new EmaUser();
        }
        return instance;
    }


    public String getNickName() {
        if (TextUtils.isEmpty(nickName)) {
            return "";
        } else {
            return nickName;
        }
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAllianceUid() {
        return mAlienceUid;
    }

    public void setAllianceUid(String aUid) {
        this.mAlienceUid = aUid;
    }

    public void setmUid(String uid) {
        this.mUid = uid;
    }

    public String getmUid() {
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

    public boolean getIsLogin() {
        return isLogin;
    }


    /**
     * 退出登录后，清空所有用户信息
     */
    public void clearUserInfo() {
        instance = null;  //这样再getInstance就得到的是一个空的实例 妙
    }

    /**
     * 统计游戏角色信息
     */
    public void submitLoginGameRole(Map<String, String> data) {
        String roleId_R = data.get("roleId");
        String roleName_R = data.get("roleName");
        String roleLevel_R = data.get("roleLevel");
        String zoneId_R = data.get("zoneId");
        String dataType_R = data.get("dataType");
        String ext_R = data.get("ext");

        ULocalUtils.spPut(EmaSDK.mActivity,"roleId_R",roleId_R);
        ULocalUtils.spPut(EmaSDK.mActivity,"roleName_R",roleName_R);
        ULocalUtils.spPut(EmaSDK.mActivity,"roleLevel_R",roleLevel_R);
        ULocalUtils.spPut(EmaSDK.mActivity,"zoneId_R",zoneId_R);
        ULocalUtils.spPut(EmaSDK.mActivity,"dataType_R",dataType_R);
        ULocalUtils.spPut(EmaSDK.mActivity,"ext_R",ext_R);

        //这个稍微特殊点，因为这个sdk有3种4399和小米都在，所以通过这样的方式来给anysdk提交这个(这是登录处的，支付处的自己取)
        if(!"000066".equals(ULocalUtils.getChannelId(EmaSDK.mActivity))&&!"000108".equals(ULocalUtils.getChannelId(EmaSDK.mActivity))){
            if(AnySDKUser.getInstance().isFunctionSupported("submitLoginGameRole")){
                Map<String, String> map = new HashMap<>();
                map.put("dataType", dataType_R);
                map.put("roleId", roleId_R);
                map.put("roleName", roleName_R);
                map.put("roleLevel", roleLevel_R);
                map.put("zoneId", zoneId_R);
                map.put("zoneName", "服务器"+zoneId_R);
                map.put("balance", "66");
                map.put("partyName", "emaUnion");
                map.put("vipLevel", "1");
                map.put("roleCTime", "-1");
                map.put("roleLevelMTime", "-1");

                AnySDKParam param = new AnySDKParam(map);
                AnySDKUser.getInstance().callFunction("submitLoginGameRole",param);
            }
        }
    }

}
