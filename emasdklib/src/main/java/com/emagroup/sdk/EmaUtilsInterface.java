package com.emagroup.sdk;

import org.json.JSONObject;

/**
 * Created by Administrator on 2016/12/13.
 */

public interface EmaUtilsInterface {
    void realInit(final EmaSDKListener listener, JSONObject data);
    void onBackPressed(EmaBackPressedAction action);
    void realLogin(EmaSDKListener listener);
    void logout();
    void swichAccount();
    void realPay(EmaSDKListener listener, EmaPayInfo emaPayInfo);
    void doPayPre(EmaSDKListener listener);
    void doShowToolbar();
    void doHideToobar();
    void onResume();
    void onPause();
    void onStop();
    void onRestart();
    void onDestroy();
}
