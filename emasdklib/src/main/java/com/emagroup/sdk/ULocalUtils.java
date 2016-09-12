package com.emagroup.sdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.anysdk.framework.java.AnySDK;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

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
        public static int getIntegerFromMetaData(Context context, String key){
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
     * 得到的是ema定义的appkey，而不是anysdk的apkey
     * @param context
     * @return
     */
    public static String getAppKey(Context context){
        return ULocalUtils.EmaSdkInfo.getIntegerFromMetaData(context,"EMA_APP_ID")+"";
    }

    /**
     * 我们和anysdk对于渠道id的定义一样（即使用anysdk的渠道号）
     * @return
     */
    public static String getAllienceId(){
        String channelID = AnySDK.getInstance().getChannelId();
        return channelID;
    }


    public static String getIMEI(Context context){
        //1.获取deviceID 其实是IMEI
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();

        /*//2.获取Android ID  不可靠，可能为null，如果恢复出厂设置会改变，root的话可以任意改变
        mSzAndroidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);*/
    }

}
