package com.emagroup.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.emagroup.sdk.impl.ShareDialog;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class ULocalUtils {

    public static class EmaSdkInfo {

        private static NamedNodeMap NodeMap;

        //
        //初始化，读取XML
        public static void readXml(String filename, Context context) {
            try {
                InputStream mXmlResourceParser = context.getAssets().open(filename);
                DocumentBuilder builder = null;
                DocumentBuilderFactory factory = null;
                factory = DocumentBuilderFactory.newInstance();
                try {
                    builder = factory.newDocumentBuilder();
                } catch (ParserConfigurationException e1) {
                    // TODO Auto-generated catch block
                    Log.e("ULocalUtils", "factory创建失败");
                    e1.printStackTrace();
                }
                try {
                    org.w3c.dom.Document document = builder.parse(mXmlResourceParser);
                    Element root = document.getDocumentElement();
                    NodeList nodes = root.getElementsByTagName("channel");
                    NodeMap = ((Element) nodes.item(0)).getAttributes();

                    //遍历xml
                    int leng = NodeMap.getLength();
                    Log.e("ULocalUtils", filename + "中节点数量： " + leng);
                    for (int i = 0; i < NodeMap.getLength(); i++) {
                        String mString5 = NodeMap.item(i).getNodeName();
                        String mString6 = NodeMap.item(i).getNodeValue();
                        Log.e("ULocalUtils", "遍历" + filename + "中节点： " + mString5 + mString6);
                    }
                    //遍历end


                } catch (SAXException e) {
                    // TODO Auto-generated catch block
                    Log.e("", "解析" + filename + "失败2");
                    e.printStackTrace();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e("ULocalUtils", "读取" + filename + "出错！！！");
                e.printStackTrace();
            }
        }

        //获取
        public static String getStringFromXML(String str) {
            Node mNode = NodeMap.getNamedItem(str);
            String mString = mNode.getNodeValue();
            if (mString == null) {
                Log.e("", str + "为空");
            }
            return mString;
        }


        /**
         * 根据key获取metaData的string类型的数据
         *
         * @param context
         * @param key
         * @return
         */
        public static String getStringFromMetaData(Context context, String key) {
            ApplicationInfo ai;
            String value = null;
            try {
                ai = context.getPackageManager().getApplicationInfo(
                        context.getPackageName(), PackageManager.GET_META_DATA);
                Bundle bundle = ai.metaData;
                value = bundle.getString(key);
            } catch (Exception e) {
                Log.e("getStringFromMetaData", "参数设置错误, 请检查！");
                e.printStackTrace();
            }
            return value;
        }


        /**
         * 根据key获取metaData的Integer类型的数据
         *
         * @param context
         * @param key
         * @return
         */
        public static int getIntegerFromMetaData(Context context, String key) {
            ApplicationInfo ai;
            int value = 0;
            try {
                ai = context.getPackageManager().getApplicationInfo(
                        context.getPackageName(), PackageManager.GET_META_DATA);
                Bundle bundle = ai.metaData;
                value = bundle.getInt(key);
            } catch (Exception e) {
                Log.e("ullocalUtils", "参数设置错误, 请检查！");
                e.printStackTrace();
            }
            return value;
        }

    }

    /**
     * 获得官方定义的appid
     *
     * @param context
     * @return
     */
    public static String getAppId(Context context) {
        return ULocalUtils.EmaSdkInfo.getStringFromMetaData(context, "EMA_APP_ID").substring(1);
    }

    /**
     * 我们和anysdk对于渠道id的定义一样（即使用anysdk的渠道号）
     *
     * @return
     */
    public static String getChannelId(Context context) {
        String channelId = ULocalUtils.EmaSdkInfo.getStringFromMetaData(context, "EMA_CHANNEL_ID").substring(1);
        if (Integer.parseInt(channelId) == 26) {  //说明没有改，就是原包的值(以后有可能==||70等)，则读下面这个（anysdk的）；否则就是单接的那种，读EMA。。这个
            channelId = ULocalUtils.EmaSdkInfo.getStringFromMetaData(context, "ASC_ChannelID").substring(1);
        }
        return channelId;
    }

    public static String getChannelTag(Context context) {
        return ULocalUtils.EmaSdkInfo.getStringFromMetaData(context, "EMA_CHANNEL_TAG").substring(1);
    }


    public static String getDeviceId(Context context) {
        //1.获取deviceID 其实是IMEI
        // TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // return tm.getDeviceId();

        /*//2.获取Android ID  不可靠，可能为null，如果恢复出厂设置会改变，root的话可以任意改变
       // mSzAndroidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);*/

        TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        String DEVICE_ID = tm.getDeviceId();
        String MacAddress = manager.getConnectionInfo().getMacAddress();
        String AndroidSerialNum = android.os.Build.SERIAL;

        if (TextUtils.isEmpty(DEVICE_ID)) {
            String oneIdNoMd5 = MacAddress + AndroidSerialNum;
            String oneId = MD5(oneIdNoMd5).substring(8, 24);
            return oneId;
        }
        Log.e("DEVICE_ID" + "MAC", DEVICE_ID + "......" + MacAddress + "..." + AndroidSerialNum);
        return DEVICE_ID;
    }

    /**
     * 获取应用名称
     *
     * @param context
     * @return
     */
    public static String getApplicationName(Context context) {
        PackageManager pm = context.getPackageManager();
        String appName = context.getApplicationInfo().loadLabel(pm).toString();
        return appName;
    }

    /**
     * 获得appicon 的 id
     *
     * @param context
     * @return
     */
    public static int getAppIconId(Context context) {

        PackageManager pm = context.getPackageManager();
        String packageName = context.getApplicationInfo().packageName;

        Intent intent = pm.getLaunchIntentForPackage(packageName);
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null) {
            return resolveInfo.getIconResource();
        } else {
            return 0;
        }
    }

    /**
     * 分享弹窗，进一步选择哪个渠道分享
     */
    public static void doShare(Activity activity, EmaSDKListener listener, Bitmap bitmap) {
        ShareDialog shareDialog = ShareDialog.create(activity);
        shareDialog.setCallbackListener(listener);
        shareDialog.setShareBitMap(bitmap);
        shareDialog.showDialog();
    }

    public static void doShare(Activity activity, EmaSDKListener listener, String text) {
        ShareDialog shareDialog = ShareDialog.create(activity);
        shareDialog.setCallbackListener(listener);
        shareDialog.setShareString(text);
        shareDialog.showDialog();
    }

    public static void doShare(Activity activity, EmaSDKListener listener, String url, String title, String description, Bitmap bitmap) {
        ShareDialog shareDialog = ShareDialog.create(activity);
        shareDialog.setCallbackListener(listener);
        shareDialog.setShareWebPage(url,title,description,bitmap);
        shareDialog.showDialog();
    }


    /**
     * 获取versioncode 整数
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        int versionCode = 0;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }


    /**
     * 返回md5加密后的字符串
     *
     * @param str
     * @return
     */
    public static String MD5(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
            byte bytes[] = messageDigest.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < bytes.length; i++)
                if (Integer.toHexString(0xff & bytes[i]).length() == 1)
                    sb.append("0").append(Integer.toHexString(0xff & bytes[i]));
                else
                    sb.append(Integer.toHexString(0xff & bytes[i]));
            return sb.toString().toUpperCase();
        } catch (Exception e) {
        }
        return "";
    }

    public static final String SP_FILE_NAME = "allience_sp_file";

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param context
     * @param key
     * @param object
     */
    public static void spPut(Context context, String key, Object object) {

        SharedPreferences sp = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (null == object) {
            return;
        } else if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }

        editor.commit();
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param context
     * @param key
     * @param defaultObject
     * @return
     */
    public static Object spGet(Context context, String key, Object defaultObject) {
        SharedPreferences sp = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);

        if (defaultObject instanceof String) {
            return sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sp.getLong(key, (Long) defaultObject);
        }

        return null;
    }

}
