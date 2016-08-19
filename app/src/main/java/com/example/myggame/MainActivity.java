package com.example.myggame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anysdk.framework.PluginWrapper;
import com.example.sdk.emasdk.EmaCallBackConst;
import com.example.sdk.emasdk.EmaSDK;
import com.example.sdk.emasdk.EmaSDKIAP;
import com.example.sdk.emasdk.EmaSDKListener;
import com.example.sdk.emasdk.EmaSDKUser;

import java.util.ArrayList;

public class MainActivity extends Activity implements OnClickListener {

    private Button btLogin;
    protected boolean isSuccess;
    private Handler uiHandler;
    private Button btPay;
    private LinearLayout myLayout;
    private TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiHandler = new Handler();
        tvName = (TextView) findViewById(R.id.tv_login);
        btLogin = (Button) findViewById(R.id.bt_login);
        btPay = (Button) findViewById(R.id.bt_pay);


        EmaSDK.getInstance().init(this, new EmaSDKListener() {
            @Override
            public void onCallBack(int arg0, String arg1) {
                switch (arg0) {
                    case EmaCallBackConst.INITSUCCESS://初始化SDK成功回调
                        isSuccess = true;
                        Toast.makeText(MainActivity.this, "sdk初始化成功", Toast.LENGTH_LONG).show();
                        break;
                    case EmaCallBackConst.INITFALIED://初始化SDK失败回调
                        break;
                    case EmaCallBackConst.LOGINSUCCESS://登陆成功回调
                        showDialog("登陆成功\n设备id为\n----");
                        EmaSDKUser.getInstance().getUserID();
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
                    /*case UserWrapper.ACTION_RET_REALNAMEREGISTER://实名注册回调
                        break;
                    case UserWrapper.ACTION_RET_ACCOUNTSWITCH_SUCCESS://切换账号成功回调
                        break;
                    case UserWrapper.ACTION_RET_ACCOUNTSWITCH_FAIL://切换账号失败回调
                        break;
                    case UserWrapper.ACTION_RET_OPENSHOP://应用汇特有回调，接受到该回调调出游戏商店界面
                        break;
                    default:
                        break;*/
                }
            }
        });

        //initPayListner();

        tvName.setOnClickListener(this);
        btLogin.setOnClickListener(this);
        btPay.setOnClickListener(this);

        Log.e("++++++++++", Thread.currentThread().getName());
    }

    /*private void initPayListner() {
        */

    /**
     * 为支付系统设置监听
     *//*
        EmaSDKIAP.getInstance().setListener();
    }*/
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_login:
                break;
            case R.id.bt_login:
                if (isSuccess) {
                    EmaSDKUser.getInstance().login();
                    //AnySDKUser.getInstance().login();
                } else {
                    Toast.makeText(this, "sdk未初始化成功", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.bt_pay:
                EmaSDKIAP iap = EmaSDKIAP.getInstance();
                ArrayList<String> idArrayList = iap.getPluginId();
                //if (idArrayList.size() == 1) {
                iap.payForProduct(idArrayList.get(0), DataManager
                        .getInstance().getProductionInfo(), new EmaSDKListener() {

                    @Override
                    public void onCallBack(int arg0, String arg1) {
                        Log.d(String.valueOf(arg0), arg1);
                        String temp = "fail";
                        switch (arg0) {
                            case EmaCallBackConst.PAYSUCCESS:// 支付成功回调
                                temp = "Success";
                                showDialog("pay successful---");
                                break;
                            case EmaCallBackConst.PAYFALIED:// 支付失败回调
                                showDialog("pay failed---");
                                break;
                            case EmaCallBackConst.PAYCANELI:// 支付取消回调
                                //showDialog(temp, "Cancel");
                                break;

                        }
                    }
                });
               /* } else {
                    *//**
             * 多支付
             *//*
                    ChoosePayMode(idArrayList);
                }*/
                break;
        }


    }

   /* */

    /**
     * @param @param payMode
     * @return void
     * @throws
     * @Title: ChoosePayMode
     * @Description: 多支付调用方法
     *//*
    public void ChoosePayMode(ArrayList<String> payMode) {
        myLayout = new LinearLayout(this);
        OnClickListener onclick = new OnClickListener() {

            @Override
            public void onClick(View v) {
                EmaSDKIAP.getInstance().payForProduct((String) v.getTag(), DataManager.getInstance().getProductionInfo());
            }
        };
        for (int i = 0; i < payMode.size(); i++) {
            Button button = new Button(this);
            String res = "Channel" + payMode.get(i);
            //button.setText(getResourceId(res, "string"));
            button.setText("xxxxx");
            button.setOnClickListener(onclick);
            button.setTag(payMode.get(i));
            myLayout.addView(button);
        }

        AlertDialog.Builder dialog02 = new AlertDialog.Builder(this);
        dialog02.setView(myLayout);
        dialog02.setTitle("UI PAY");


        dialog02.show();
    }*/
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
    protected void onDestroy() {
        PluginWrapper.onDestroy();
        EmaSDK.getInstance().release();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        PluginWrapper.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        PluginWrapper.onResume();
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PluginWrapper.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        PluginWrapper.onNewIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onStop() {
        PluginWrapper.onStop();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        PluginWrapper.onRestart();
        super.onRestart();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
