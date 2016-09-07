package com.emagroup.sdk;

import com.anysdk.framework.java.AnySDKUser;

/**
 * Created by Administrator on 2016/9/7.
 */
public class EmaUser {

    private static EmaUser instance;

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
}
