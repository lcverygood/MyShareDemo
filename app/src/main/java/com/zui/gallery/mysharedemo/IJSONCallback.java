package com.zui.gallery.mysharedemo;

/**
 * 自定义volley网络请求回调监听接口
 * @author zcf
 *
 */
public abstract class IJSONCallback {
	
	/**
	 * 成功回调方法
	 * @param json
	 */
	public abstract void onSuccess(Object json);
	
	/**
	 * 失败回调方法
	 */
	public abstract void onFailure(int errorCode, String desc);
}
