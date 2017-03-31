package com.emagroup.sdk.emasdk;

import com.emagroup.sdk.sdkcom.EmaBackPressedAction;
import com.emagroup.sdk.sdkcom.EmaPayInfo;
import com.emagroup.sdk.sdkcom.EmaSDKListener;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Administrator on 2016/10/9.
 */
public interface EmaUtilsInterface {

    void realInit(EmaSDKListener listener, JSONObject data);

    void realLogin(EmaSDKListener listener, String userid, String deviceId);

    void doPayPre(EmaSDKListener listener);

    void realPay(EmaSDKListener listener, EmaPayInfo emaPayInfo);

    void logout();

    void swichAccount();

    void doShowToolbar();

    void doHideToobar();

    void onResume();

    void onPause();

    void onStop();

    void onDestroy();

    void onBackPressed(EmaBackPressedAction action);

    void submitGameRole(Map<String, String> data);

}