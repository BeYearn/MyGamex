package com.emagroup.sdk;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.Map;

public class EmaWebviewDialog extends Dialog {

	private final int showType;//显示样式 1只有确定按钮  2确定取消按钮都有
	private final Dialog dialogFrom; //从这个dialog来
	private final int clickType;
	private final Map mContentMap;
	private final Handler mHandler;
	private ResourceManager mResourceManager;// 资源管理
	private static final int ALERT_SHOW = 13;

	//views
	private TextView mBtnSure;
	private TextView mBtnCancle;
	private WebView mWebView;

	/**
	 *
	 * @param context
	 * @param dialog
	 * @param contentMap
	 * @param showType   1 显示一个确定按钮  2 显示确定 取消
     * @param clickType  1确定按钮按下退出   2确定按钮按下顺利进入 3确定按钮按下可选更新  4确定按钮按下强制更新
     */
	public EmaWebviewDialog(Context context, Dialog dialog, Map contentMap, int showType, int clickType, Handler handler) {
		super(context, ResourceManager.getInstance(context).getIdentifier("ema_activity_dialog", "style"));
		this.showType=showType;
		this.clickType=clickType;
		this.dialogFrom=dialog;
		mResourceManager = ResourceManager.getInstance(context);
		setCancelable(false);
		setCanceledOnTouchOutside(false);
		this.mContentMap = contentMap;
		this.mHandler=handler;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();
	}
	
	private void initView() {
		setContentView(mResourceManager.getIdentifier("ema_webview_dialog", "layout"));

		mWebView = (WebView) findViewById(mResourceManager.getIdentifier("webView_dialog", "id"));//dialog显示的webview

		mBtnSure= (TextView) findViewById(mResourceManager.getIdentifier("ema_tv_sure", "id"));
		if(1==clickType){
			mBtnSure.setText("退出游戏");
		}else if(2==clickType){
			mBtnSure.setText("进入游戏");
		}
		mBtnCancle= (TextView) findViewById(mResourceManager.getIdentifier("ema_tv_cancel", "id"));

		mBtnSure.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(1==clickType){
					System.exit(0);
				}else if(2==clickType){
					EmaWebviewDialog.this.dismiss();
					dialogFrom.dismiss();
					String whichUpdate = (String) mContentMap.get("whichUpdate");
					if(!"none".equals(whichUpdate)){//有更新
						Message message = Message.obtain();
						if("hard".equals(whichUpdate)){
							message.what=ALERT_SHOW;
							message.arg1=1;  // 强更的话只有一个确定按钮
							message.arg2=2;  // 确定开始更新 取消退出
							message.obj=mContentMap;
						}else {
							message.what=ALERT_SHOW;
							message.arg1=2;
							message.arg2=1;  // 确定开始更新 取消继续进去
							message.obj=mContentMap;
						}
						mHandler.sendMessage(message);
					}
				}else if (3==clickType){
					//开始强制或者可选更新
					//ToastHelper.toast(c);
				}
			}
		});

		mBtnCancle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(4==clickType){
					System.exit(0);
				}else if(3==clickType){

				} else{
					EmaWebviewDialog.this.dismiss();
					dialogFrom.dismiss();
				}
			}
		});


		if(1==showType){    // 将取消按钮隐藏
			mBtnSure.setVisibility(View.VISIBLE);
			mBtnCancle.setVisibility(View.GONE);
		}else if(2==showType){  //两个都显示
			mBtnSure.setVisibility(View.VISIBLE);
			mBtnCancle.setVisibility(View.VISIBLE);
		}
	}

	private void initData(){
		String contentUrl = (String) mContentMap.get("maintainContent");
		mWebView.loadUrl(contentUrl);
		/*mWebView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});*/
	}
}
