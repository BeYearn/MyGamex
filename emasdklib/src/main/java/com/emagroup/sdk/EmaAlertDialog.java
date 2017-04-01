package com.emagroup.sdk;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

public class EmaAlertDialog extends Dialog {

	private final int showType;//显示样式 1只有确定按钮  2确定取消按钮都有
	private final Dialog dialogFrom; //从这个dialog来
	private final int clickType;
	private final Map mContentMap;
	private final Context mContext;
	private ResourceManager mResourceManager;// 资源管理

	//views
	private TextView mTxtPromptView;//提示语
	private ImageView mImgPromptView;//提示图片
	private TextView mBtnSure;
	private TextView mBtnCancle;

	/**
	 *
	 * @param context
	 * @param dialog
	 * @param contentMap
	 * @param showType   1 显示一个确定按钮  2 显示确定 取消
     * @param clickType  1确定按钮按下退出   2确定按钮按下顺利进入 3确定按钮按下可选更新  4确定按钮按下强制更新
     */
	public EmaAlertDialog(Context context, Dialog dialog, Map contentMap, int showType, int clickType) {
		super(context, ResourceManager.getInstance(context).getIdentifier("ema_activity_dialog", "style"));
		this.mContext=context;
		this.showType=showType;
		this.clickType=clickType;
		this.dialogFrom=dialog;
		mResourceManager = ResourceManager.getInstance(context);
		setCancelable(false);
		setCanceledOnTouchOutside(false);
		this.mContentMap = contentMap;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();
	}
	
	private void initView() {
		setContentView(mResourceManager.getIdentifier("ema_alert_dialog", "layout"));

		mImgPromptView = (ImageView) findViewById(mResourceManager.getIdentifier("ema_img_pay_result", "id"));//那个小图标
		mTxtPromptView = (TextView) findViewById(mResourceManager.getIdentifier("ema_txt_content", "id"));//dialog显示的内容

		mBtnSure= (TextView) findViewById(mResourceManager.getIdentifier("ema_tv_sure", "id"));
		mBtnCancle= (TextView) findViewById(mResourceManager.getIdentifier("ema_tv_cancel", "id"));

		mBtnSure.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//开始更新
				//ToastHelper.toast(mContext,"开始更新（测试）。。");
				if(!TextUtils.isEmpty((String) mContentMap.get("updateUrl"))){
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					Uri upadateUrl =Uri.parse((String) mContentMap.get("updateUrl"));  // url必须写全（"https://www.baidu.com"）
					intent.setData(upadateUrl);
					mContext.startActivity(intent);
				}

			}
		});

		mBtnCancle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(1==clickType){
					EmaAlertDialog.this.dismiss();
					dialogFrom.dismiss();
				}else if(2==clickType){
					System.exit(0);
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

		/*if()
		String contentStr = (String) mContentMap.get("maintainContent");

		int drawableId = mResourceManager.getIdentifier("ema_prompt_paywarn", "drawable");

		if(!UCommUtil.isStrEmpty(contentStr)){
			mTxtPromptView.setText(contentStr);
		}
		mImgPromptView.setImageResource(drawableId);*/
		mTxtPromptView.setText("检测到新版本，请赶紧更新");
	}
}
