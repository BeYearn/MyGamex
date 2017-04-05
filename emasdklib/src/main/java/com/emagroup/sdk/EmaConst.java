package com.emagroup.sdk;

/**
 * EMA平台供外部调用的一些常量
 * @author zhangyang
 *
 */
public class EmaConst {



//------------------------------------------------------------------------------------
	public static final String EMA_PAYINFO_PRODUCT_ID = "product_id";  //商品id
	public static final String EMA_PAYINFO_PRODUCT_COUNT = "product_count"; //商品数量
	public static final String EMA_GAMETRANS_CODE="gameTransCode"; //游戏数据透传字段



	//----------------------------------------------------------------------------

	public static final String EMA_BC_GETCHANNEL_OK_ACTION = "ema_getchannel_ok";  //获取渠道key信息成功
	public static final String EMA_BC_LOGIN_OK_ACTION = "ema_login_ok";
	public static final String EMA_BC_CHANNEL_INFO = "initData";


	//---------------------------------------------------------------------------------广播控制菊花窗开关

	public static final String EMA_BC_PROGRESS_ACTION = "broadcast.progress.close";
	public static final String EMA_BC_PROGRESS_STATE = "progressState";
	public static final String EMA_BC_PROGRESS_CLOSE = "bc_progress_close";
	public static final String EMA_BC_PROGRESS_START = "bc_progress_start";
}
