package com.emagroup.example;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.emagroup.sdk.EmaSDK;
import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaConst;
import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.EmaUser;
import com.emagroup.sdk.ToastHelper;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends Activity implements OnClickListener {

    private Button btLogin;
    protected boolean isSuccess;
    private Handler uiHandler;
    private Button btPay;
    private LinearLayout myLayout;
    private TextView tvName;
    private Button btLogout;
    private Button btShowBar;
    private Button btHideBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiHandler = new Handler();
        tvName = (TextView) findViewById(R.id.tv_login);
        btLogin = (Button) findViewById(R.id.bt_login);
        btPay = (Button) findViewById(R.id.bt_pay);
        btLogout = (Button) findViewById(R.id.bt_logout);
        btShowBar = (Button) findViewById(R.id.bt_showbar);
        btHideBar = (Button) findViewById(R.id.bt_hidebar);

        //EmaSDK.getInstance().init("6cdd60ea0045eb7a6ec44c54d29ed402",this, new EmaSDKListener() {      //
        EmaSDK.getInstance().init("864ed48309c7f9a259f769b92cc52814", this, new EmaSDKListener() {       //20012
            //EmaSDK.getInstance().init("5600441101c8818c4480d3c503742a3b",this, new EmaSDKListener() {      //20015
            @Override
            public void onCallBack(int arg0, String arg1) {
                switch (arg0) {
                    case EmaCallBackConst.INITSUCCESS://初始化SDK成功回调
                        isSuccess = true;
                        Log.e("Mainactivity", "sdk初始化成功");
                        break;
                    case EmaCallBackConst.INITFALIED://初始化SDK失败回调
                        Log.e("Mainactivity", "sdk初始化失败");
                        break;
                    case EmaCallBackConst.LOGINSUCCESS://登陆成功回调
                        showDialog("登陆成功\n设备id为\n----");
                        Log.e("Mainactivity", EmaUser.getInstance().getNickName());
                        Log.e("Mainactivity", EmaUser.getInstance().getAllianceUid());
                        Log.e("Mainactivity", EmaSDK.getInstance().getChannelId());

                        Map<String,String > param=new HashMap<String, String>();
                        param.put(EmaConst.SUBMIT_ROLE_ID,"00001");
                        param.put(EmaConst.SUBMIT_ROLE_NAME,"emasdk");
                        param.put(EmaConst.SUBMIT_ROLE_LEVEL,"11");
                        param.put(EmaConst.SUBMIT_ZONE_ID,"20003");
                        param.put(EmaConst.SUBMIT_ZONE_NAME,"emasever");
                        param.put(EmaConst.SUBMIT_ROLE_CT,"1234567899");
                        param.put(EmaConst.SUBMIT_DATA_TYPE,"0");
                        param.put(EmaConst.SUBMIT_EXT,"99999");
                        EmaSDK.getInstance().submitGameRole(param);

                        break;
                    case EmaCallBackConst.LOGINCANELL://登陆取消回调
                        break;
                    case EmaCallBackConst.LOGINFALIED://登陆失败回调
                        Log.e("++++++++++", Thread.currentThread().getName());
                        showDialog("登陆失败");
                        break;
                    case EmaCallBackConst.LOGOUTSUCCESS://登出成功回调
                        break;
                    case EmaCallBackConst.LOGOUTFALIED://登出失败回调
                        break;
                    case EmaCallBackConst.ACCOUNTSWITCHSUCCESS:
                        break;
                    case EmaCallBackConst.ACCOUNTSWITCHFAIL:
                        break;
                }
            }
        });

        EmaSDK.getInstance().doSetRecivePushListner(new EmaSDKListener() {
            @Override
            public void onCallBack(int resultCode, String data) {
                if (resultCode == EmaCallBackConst.RECIVEMSG_MSG) {
                    // TODO:  data为拿到的推送数据,自行处理
                    Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();
                }
            }
        });
        tvName.setOnClickListener(this);
        btLogin.setOnClickListener(this);
        btPay.setOnClickListener(this);
        btLogout.setOnClickListener(this);
        btShowBar.setOnClickListener(this);
        btHideBar.setOnClickListener(this);

        Log.e("++++++++++", Thread.currentThread().getName());
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_login:   // 点击那个字，现在是登出功能
                EmaSDK.getInstance().doSwichAccount();
                break;
            case R.id.bt_login:
                EmaSDK.getInstance().doLogin();
                /*if (isSuccess) {
                } else {
                    Toast.makeText(this, "sdk未初始化成功", Toast.LENGTH_LONG).show();
                }*/
                break;
            case R.id.bt_logout:
                EmaSDK.getInstance().doLogout();
                break;
            case R.id.bt_showbar:
                EmaSDK.getInstance().doShowToolbar();
                break;
            case R.id.bt_hidebar:
                EmaSDK.getInstance().doHideToobar();
                break;
            case R.id.bt_pay:

                HashMap<String, String> payInfoMap = new HashMap<>();
                payInfoMap.put(EmaConst.EMA_PAYINFO_PRODUCT_ID, "YLZHS_1007");
                payInfoMap.put(EmaConst.EMA_PAYINFO_PRODUCT_COUNT, "1");
                payInfoMap.put(EmaConst.EMA_GAMETRANS_CODE, "游戏透传参数");
                //payInfoMap.put("Product_Name","gold");
                //payInfoMap.put("Server_Id", "13");
                //payInfoMap.put("Role_Id","1");
                //payInfoMap.put("Role_Name", "1");
                //payInfoMap.put("Role_Grade", "1");
                //payInfoMap.put("Role_Balance", "1");

                EmaSDK.getInstance().doPay(payInfoMap, new EmaSDKListener() {
                    @Override
                    public void onCallBack(int arg0, String arg1) {
                        Log.d(String.valueOf(arg0), arg1);
                        switch (arg0) {
                            case EmaCallBackConst.PAYSUCCESS:// 支付成功回调
                                showDialog("anysdk pay successful---");
                                break;
                            case EmaCallBackConst.PAYFALIED:// 支付失败回调
                                showDialog("anysdk pay failed---");
                                break;
                            case EmaCallBackConst.PAYCANELI:// 支付取消回调
                                showDialog("anysdk pay cancle");
                                break;
                        }
                    }
                });
                break;
        }


    }

    private void showDialog(String str) {
        final String curMsg = str;
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                //dialog参数设置
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);  //先得到构造器
                builder.setTitle("提示"); //设置标题
                //builder.setMessage("是否确认退出?"); //设置内容
                builder.setIcon(R.drawable.ic_launcher);//设置图标，图片id即可
                //设置列表显示，注意设置了列表显示就不要设置builder.setMessage()了，否则列表不起作用。
        /*builder.setItems(items,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, items[which], Toast.LENGTH_SHORT).show();

            }
        });*/
                builder.setMessage(curMsg);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setCancelable(false);
                builder.create().show();
            }
        });
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        EmaSDK.getInstance().onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EmaSDK.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        EmaSDK.getInstance().onRestart();
    }

    @Override
    protected void onDestroy() {
        EmaSDK.getInstance().onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        EmaSDK.getInstance().onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        EmaSDK.getInstance().onResume();
        super.onResume();
    }


    @Override
    protected void onStop() {
        EmaSDK.getInstance().onStop();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        EmaSDK.getInstance().onBackPressed(new EmaBackPressedAction() {
            @Override
            public void doBackPressedAction() {
                //游戏需要做的逻辑
                ToastHelper.toast(MainActivity.this, "我是游戏自己的逻辑xxxxxxxxxxxxx");
                //MainActivity.super.onBackPressed();
            }
        });
    }
}
