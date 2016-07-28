package com.example.ehsy.emasdk;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.anysdk.framework.java.AnySDK;
import com.anysdk.framework.java.AnySDKUser;

/**
 * Created by Young on 2016/7/9.
 */
public class EmaSdk {
    private static EmaSdk instance = null;
    private static Activity mActivity = null;

    public EmaSdk() {
    }

    public static EmaSdk getInstance() {
        if (instance == null) {
            return new EmaSdk();
        }
        return instance;
    }


    public void init(Activity activity, String appKey, String appSecret, String privateKey, String authLoginServer) {
        this.mActivity = activity;
        AnySDK.getInstance().init(activity, appKey, appSecret, privateKey, authLoginServer);
    }

    public String login() {
        //1.获取deviceID 其实是IMEI
        TelephonyManager tm = (TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);
        String DEVICE_ID = tm.getDeviceId();
        //2.获取Android ID  不可靠，可能为null，如果恢复出厂设置会改变，root的话可以任意改变
        String m_szAndroidID = Settings.Secure.getString(mActivity.getContentResolver(), Settings.Secure.ANDROID_ID);

        AnySDKUser.getInstance().login();
        return DEVICE_ID;
    }



    /*private String getUniqueId() {
        String m_szLongID = m_szImei + m_szDevIDShort
                + m_szAndroidID + m_szWLANMAC + m_szBTMAC;
        // compute md5
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
        // get md5 bytes
        byte p_md5Data[] = m.digest();
        // create a hex string
        String m_szUniqueID = new String();
        for (int i = 0; i < p_md5Data.length; i++) {
            int b = (0xFF & p_md5Data[i]);
        // if it is a single digit, make sure it have 0 in front (proper padding)
            if (b <= 0xF)
                m_szUniqueID += "0";
        // add number to string
            m_szUniqueID += Integer.toHexString(b);
        }   // hex string to uppercase
        m_szUniqueID = m_szUniqueID.toUpperCase();
    }*/
}
