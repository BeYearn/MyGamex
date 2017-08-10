package com.emagroup.sdk;

/**
 * EMA平台供外部调用的一些常量
 * @author zhangyang
 *
 */
public class EmaConst {


	//----------------------------------------------------------------------------------submitgamerole
	public static final String SUBMIT_ROLE_ID = "roleId";//角色信息
	public static final String SUBMIT_ROLE_NAME = "roleName";//角色名称
	public static final String SUBMIT_ROLE_LEVEL = "roleLevel";//角色级别
	public static final String SUBMIT_ZONE_ID = "zoneId";//服务器id
	public static final String SUBMIT_ZONE_NAME = "zoneName";//服务器名称
	public static final String SUBMIT_ROLE_CT = "roleCreateTime";//创角时间
	public static final String SUBMIT_DATA_TYPE = "dataType";//创角时间
	public static final String SUBMIT_EXT = "ext";//创角时间


//------------------------------------------------------------------------------------
	public static final String EMA_PAYINFO_PRODUCT_ID = "product_id";  //商品id
	public static final String EMA_PAYINFO_PRODUCT_COUNT = "product_count"; //商品数量
	public static final String EMA_GAMETRANS_CODE="gameTransCode"; //游戏数据透传字段



	//----------------------------------------------------------------------------

	public static final String EMA_BC_GETCHANNEL_OK_ACTION = "ema_getchannel_ok";  //获取渠道key信息成功
	public static final String EMA_BC_CHANNEL_INFO = "initData";


	//---------------------------------------------------------------------------------广播控制菊花窗开关

	public static final String EMA_BC_PROGRESS_ACTION = "broadcast.progress.close";
	public static final String EMA_BC_PROGRESS_STATE = "progressState";
	public static final String EMA_BC_PROGRESS_CLOSE = "bc_progress_close";
	public static final String EMA_BC_PROGRESS_START = "bc_progress_start";

	//--------------------------------------------------------------------------------从渠道那里传来的uid和nikeName

	public static final String ALLIANCE_UID = "allianceUid";
	public static final String NICK_NAME = "nickName";

}
