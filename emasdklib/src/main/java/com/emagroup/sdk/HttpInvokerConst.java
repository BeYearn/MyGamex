package com.emagroup.sdk;

public class HttpInvokerConst {

	public static final String RESULT_CODE = "errno";

	public static final int SDK_RESULT_SUCCESS = 0;//表示请求数据或操作成功
	public static final int SDK_RESULT_FAILED=1;

	public static final int SDK_RESULT_ERROR=9; //各种失败 比如请求超时啥的

	public static final int SDK_RESULT_FAILED_SIGIN_ERROR = 3006;//签名失败
	
	// 登录返回标记
	public static final int SDK_RESULT_PASSW_ERROR = 2007;// 表示密码错误
	public static final int SDK_RESULT_ACCOUNT_NOT_EXIST = 2003;// 表示用户名不存在
	public static final int LOGIN_RESULT_URL_IS_NULL = 4000;// 表示传递的curl为空
	public static final int LOGIN_CHECK_SID_RESULT_SID_OVER = 3070;// Sid过期
	public static final int LOGIN_CHECK_SID_RESULT_SID_OVER_1 = 3071;// Sid过期
	//手机注册登录返回标记
	public static final int LOGIN_PHONE_LOGIN_AUTH_CODE_ERROR = 2009;//验证码错误
	
	// 注册返回标记
	public static final int REGISTER_ACCOUNT_EXIST = 2005;// 账户已存在
	public static final int REGISTER_ERROR_EMAIL = 2036;//邮箱格式错误
	
	//获取验证的返回标记
	public static final int SEND_PHONE_CODE_FAILED = 2022;//发送验证码失败
	//NOTE 可能会有问题，最好个跟服务器沟通一下
	public static final int SEND_PHONE_CODE_FAILED_TOO_OFFTEN = 2044;//发送验证码过于频繁
	public static final int SEND_PHONE_CODE_FAILED_OVER_MAX = 2043;//验证码发送量达到今日的最大限量
	public static final int SEND_PHONE_CODE_FAILED_OVER_MAX_1 = 2054;//like 2043
	
	//支付相关返回标记
	public static final int PAY_RECHARGE_FAILED_SHORT_MONEY = 4018;//余额不足
	public static final int PAY_RECHARGE_FAILED_PASSW_ERROR = 4017;//密码错误
	public static final int PAY_RECHARGE_FAILED_PASSW_ERROR_OVER = 4044;//密码错误次数过多
	public static final int PAY_RECHARGE_FAILED_ORDERID_REPEAT = 4007;//订单号重复
	//卡支付相关返回标记
	public static final int PAY_RECHARGE_CARD_ON_PAYING = 900000;//等待付款中
	public static final int PAY_RECHARGE_CARD_FAILED = 900002;//付款失败
	public static final int PAY_RECHARGE_CARD_ORDER_OVER_TIME = 900003;//订单过期
	public static final int PAY_RECHARGE_CARD_ORDER_UNSAVE_PAYED = 900009;//已支付风险订单
	public static final int PAY_RECHARGE_CARD_ORDER_CHECKING = 900010;//风控审核中
	public static final int PAY_RECHARGE_CARD_ORDER_UNSAVE_REFUND = 900011;//风险订单支付拒绝，退款中
	public static final int PAY_RECHARGE_CARD_FAILED_UNKNOW = 900012;//失败，原因未知
	
	//用户注册登录相关
	public static final int SUCCESSFULL=0;
}
