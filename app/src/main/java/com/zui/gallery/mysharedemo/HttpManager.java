package com.zui.gallery.mysharedemo;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

//import com.google.gson.Gson;
//import com.xy.sfa.R;
//import com.xy.sfa.app.ConstantConfig;
//import com.xy.sfa.app.SkinSettingManager;
//import com.xy.sfa.constant.HttpConstant;
//import com.xy.sfa.util.AndroidUtil;
//import com.xy.sfa.util.GlobalUtil;
//import com.xy.sfa.util.LogUtil;
//import com.xy.sfa.util.ParseMD5;
//import com.xy.sfa.util.ToastUtils;

/**
 * 网络请求管理类
 *
 * @author zcf
 */
public class HttpManager<T> {

    private String TAG = "HttpManager";
    private RequestQueue mRequestQueue;
    private Gson gson = new Gson();

    public static boolean IS_KICK = false; // 是否被踢出

    /**
     * 单例模式
     */
    private static HttpManager INSTANCE;

    public synchronized static HttpManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HttpManager();
        }
        return INSTANCE;
    }

    /**
     * 网络请求成功回调
     *
     * @param callback
     * @param isToast
     * @param response
     */
    private void sucessCallBack(Context context, final IJSONCallback callback, Class<T> clazz, boolean isToast, JSONObject response) {
//		LogUtil.i(TAG, "onResponse:" + response.toString());
        if (callback != null) {
            try {
                if (response.has("error")) {
                    int code = response.getJSONObject("error").optInt("returnCode");
                    if (0 == code) {
                        T result = null;
                        result = gson.fromJson(response.toString(), clazz);
                        callback.onSuccess(result);

                    } else {
                        callback.onFailure(code, response.getJSONObject("error").optString("returnMessage"));
                    }
                } else {
                    int code = response.getInt("code");
                    if (200 == code) {
                        T result = null;
                        result = gson.fromJson(response.toString(), clazz);
                        callback.onSuccess(result);

                    } else if (900 == code) {
                        if (!IS_KICK) {
//                            LogUtil.i(TAG, "is_kick");
                            IS_KICK = true;
                            Intent kickIntent = new Intent();
//                            kickIntent.setAction(ConstantConfig.OTHER_LOGIN_ACTION);
                            context.sendBroadcast(kickIntent);
                        }
                    } else if (901 == code) {
//                        LogUtil.i(TAG, "freeze");
                        Intent freezeIntent = new Intent();
//                        freezeIntent.setAction(ConstantConfig.FREEZE_USER_ACTION);
                        if (response.opt("message") != null) {
                            freezeIntent.putExtra("freeze_message", response.getString("message"));
//                            LogUtil.i(TAG, "freeze:" + response.getString("message"));
                        } else {
                            freezeIntent.putExtra("freeze_message", "您的账号已被冻结");
                        }
                        context.sendBroadcast(freezeIntent);
                    } else {
                        callback.onFailure(code, response.optString("message"));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(context, "解析数据出错", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 网络请求错误回调
     *
     * @param callback
     * @param isToast
     * @param error
     */
    private void errorCallBack(Context context, final IJSONCallback callback, final boolean isToast, VolleyError error) {
        String errorStr = "网络异常";
//        LogUtil.i(TAG, "onErrorResponse:" + error.getMessage());
        if (error.networkResponse != null) {
//            LogUtil.i(TAG, "networkResponse code:" + error.networkResponse.statusCode + "| " + error.networkResponse.toString());
        }
        if (callback != null) {

            if (error.getMessage() != null) {
//                LogUtil.i(TAG, "onErrorResponse1:" + error.getMessage());
                if (error.getMessage().contains("Connection refused")) {
                    errorStr = "服务器异常，请稍后尝试";
                } else if (error.getMessage().contains("No address associated with hostname")) {
//                    errorStr = context.getString(R.string.error_no_network);
                } else if (error.getMessage().contains("JSONException")) {
                    errorStr = "服务器异常，请稍后尝试";
                }
            } else {
                if (error.networkResponse == null) {
                    errorStr = "网络链接超时，请检查网络";
                    Toast.makeText(context, "网络异常3", Toast.LENGTH_SHORT).show();
                } else {
//                    LogUtil.i(TAG, "onErrorResponse2:" + error.getMessage() + "| status:" + error.networkResponse.statusCode);
//                    LogUtil.i(TAG, "onErrorResponse3:" + error.getMessage() + "| response:" + error.networkResponse.toString());
                    errorStr = "服务器异常，请稍后尝试";
                }
            }
            callback.onFailure(498, "网络异常");
            if (isToast) {
                Toast.makeText(context, errorStr, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 网络是否可用
     *
     * @param callback
     * @param isToast
     * @return
     */
    private boolean isNetWorkAvailable(Context context, final IJSONCallback callback, final boolean isToast) {
        boolean networkAvailable = AndroidUtil.isNetworkAvailable(context, isToast);
        if (!networkAvailable) {
            if (callback != null) {
                if (isToast) {
                    if(context == null || null == context.getString(R.string.network_unavailable)){

                    } else {
                        Toast.makeText(context, context.getString(R.string.network_unavailable), Toast.LENGTH_SHORT).show();
                    }
                }
                if(context == null || null == context.getString(R.string.network_unavailable)){

                } else {
                    callback.onFailure(650, context.getString(R.string.network_unavailable));
                }
            }
        }
        return networkAvailable;
    }

    /**
     * get请求，没有toast提示
     *
     * @param url      请求的url
     * @param callback 回调接口
     */
    public void getWithNoToast(Context context, final String url, final IJSONCallback callback, Class<T> clazz) {
        get(context, url, callback, clazz, "", false, false, true);
    }

    /**
     * get请求,不需要认证，没有toast提示
     *
     * @param url      请求的url
     * @param callback 回调接口
     */
    public void getWithNoToastAndNoAuth(Context context, final String url, final IJSONCallback callback, Class<T> clazz) {
        get(context, url, callback, clazz, "", false, false, false);
    }

    /**
     * get请求
     *
     * @param url      请求的url
     * @param callback 回调接口
     */
    public void get(Context context, final String url, final IJSONCallback callback, Class<T> clazz) {
        get(context, url, callback, clazz, "", true, false, true);
    }

    /**
     * get请求,不需要认证
     *
     * @param url      请求的url
     * @param callback 回调接口
     */
    public void getNoAuth(Context context, final String url, final IJSONCallback callback, Class<T> clazz) {
        get(context, url, callback, clazz, "", true, false, false);
    }

    /**
     * get请求
     *
     * @param url      请求的url
     * @param callback 回调接口
     */
    public void get(Context context, final String url, final IJSONCallback callback, Class<T> clazz, boolean isShowLoading) {
        get(context, url, callback, clazz, "", true, isShowLoading, true);
    }

    /**
     * get请求,不需要认证
     *
     * @param url      请求的url
     * @param callback 回调接口
     */
    public void getNoAuth(Context context, final String url, final IJSONCallback callback, Class<T> clazz, boolean isShowLoading) {
        get(context, url, callback, clazz, "", true, isShowLoading, false);
    }

    /**
     * get请求, 并设置网络请求标签
     *
     * @param url      请求的url
     * @param callback 回调接口
     * @param tag      网络请求标签
     */
    public void get(Context context, final String url, final IJSONCallback callback, Class<T> clazz, String tag) {
        get(context, url, callback, clazz, tag, true, false, true);
    }

    /**
     * get请求,不需要认证, 并设置网络请求标签
     *
     * @param url      请求的url
     * @param callback 回调接口
     * @param tag      网络请求标签
     */
    public void getNoAuth(Context context, final String url, final IJSONCallback callback, Class<T> clazz, String tag) {
        get(context, url, callback, clazz, tag, true, false, false);
    }


    /**
     * get请求
     *
     * @param url           请求的url
     * @param callback      回调接口
     * @param tag           网络请求标签
     * @param isToast       是否toast提示
     * @param isShowLoading 是否显示网络请求中对话框
     * @param isAuth        是否需要认证
     */
    public void get(final Context context, final String url, final IJSONCallback callback, final Class<T> clazz, String tag,
                    final boolean isToast, final boolean isShowLoading, final boolean isAuth) {
//		LogUtil.i(TAG, "url: " + url);
        if (!isNetWorkAvailable(context, callback, isToast)) {
            return;
        }
////		LogUtil.i(TAG, "url net normal: " + url);
        try {
            JsonObjectRequest getRequest = new MyJsonObjectRequest(Request.Method.GET, url, null, new Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
//                    LogUtil.i(TAG, url + " sucess: " + response.toString());
                    sucessCallBack(context, callback, clazz, isToast, response);

                }
            }, new ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
//                    LogUtil.i(TAG, url + " failture");
                    try {
                        Log.e(TAG, error.getMessage(), error);
                        byte[] htmlBodyBytes = error.networkResponse.data;
                        Log.e(TAG, new String(htmlBodyBytes), error);
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                    errorCallBack(context, callback, isToast, error);
                }
            });
            getRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            addToRequestQueue(context, getRequest, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * post请求，没有toast提示
     *
     * @param url       请求的url
     * @param mapParams 请求的参数Json格式，注意json中如果存入float，需要转换成double,尽量不要用float，long用string代替
     * @param callback  回调接口
     */
    public void postWithNoToast(Context context, final String url, Map<String, String> mapParams,
                                final IJSONCallback callback, Class<T> clazz) {
        post(context, url, mapParams, callback, clazz, "", false, false, true);
    }

    public void postWithTag(Context context, final String url, Map<String, String> mapParams, String tag, final IJSONCallback callback, Class<T> clazz) {
        post(context, url, mapParams, callback, clazz, tag, true, false, true);
    }

    /**
     * post请求,不需要认证，没有toast提示
     *
     * @param url       请求的url
     * @param mapParams 请求的参数Json格式，注意json中如果存入float，需要转换成double,尽量不要用float，long用string代替
     * @param callback  回调接口
     */
    public void postWithNoToastAndNoAuth(Context context, final String url, Map<String, String> mapParams,
                                         final IJSONCallback callback, Class<T> clazz) {
        post(context, url, mapParams, callback, clazz, "", false, false, false);
    }

    /**
     * post请求，没有toast提示
     *
     * @param url       请求的url
     * @param mapParams 请求的参数Json格式，注意json中如果存入float，需要转换成double,尽量不要用float，long用string代替
     * @param tag       网络请求标签
     * @param callback  回调接口
     */
    public void postWithNoToastAndTag(Context context, final String url, Map<String, String> mapParams, String tag,
                                      final IJSONCallback callback, Class<T> clazz) {
        post(context, url, mapParams, callback, clazz, tag, false, false, true);
    }

    /**
     * post请求,不需要认证，没有toast提示
     *
     * @param url       请求的url
     * @param mapParams 请求的参数Json格式，注意json中如果存入float，需要转换成double,尽量不要用float，long用string代替
     * @param tag       网络请求标签
     * @param callback  回调接口
     */
    public void postWithNoToastAndTagAndNoAuth(Context context, final String url, Map<String, String> mapParams, String tag,
                                               final IJSONCallback callback, Class<T> clazz) {
        post(context, url, mapParams, callback, clazz, tag, false, false, false);
    }

    /**
     * post请求
     *
     * @param url       请求的url
     * @param mapParams 请求的参数Json格式，注意json中如果存入float，需要转换成double,尽量不要用float，long用string代替
     * @param callback  回调接口
     */
    public void post(Context context, final String url, Map<String, String> mapParams,
                     final IJSONCallback callback, Class<T> clazz) {
        post(context, url, mapParams, callback, clazz, "", true, false, true);
    }

    /**
     * post请求,不需要认证
     *
     * @param url       请求的url
     * @param mapParams 请求的参数Json格式，注意json中如果存入float，需要转换成double,尽量不要用float，long用string代替
     * @param callback  回调接口
     */
    public void postNoAuth(Context context, final String url, Map<String, String> mapParams,
                           final IJSONCallback callback, Class<T> clazz) {
        post(context, url, mapParams, callback, clazz, "", true, false, false);
    }

    /**
     * post请求
     *
     * @param url           请求的url
     * @param mapParams     请求的参数Json格式，注意json中如果存入float，需要转换成double,尽量不要用float，long用string代替
     * @param callback      回调接口
     * @param isShowLoading 是否显示加载对话框
     */
    public void post(Context context, final String url, Map<String, String> mapParams,
                     final IJSONCallback callback, Class<T> clazz, boolean isShowLoading) {
        post(context, url, mapParams, callback, clazz, "", true, isShowLoading, true);
    }

    /**
     * post请求,不需要认证
     *
     * @param url           请求的url
     * @param mapParams     请求的参数Json格式，注意json中如果存入float，需要转换成double,尽量不要用float，long用string代替
     * @param callback      回调接口
     * @param isShowLoading 是否显示加载对话框
     */
    public void postNoAuth(Context context, final String url, Map<String, String> mapParams,
                           final IJSONCallback callback, Class<T> clazz, boolean isShowLoading) {
        post(context, url, mapParams, callback, clazz, "", true, isShowLoading, false);
    }

    /**
     * post请求, 并设置网络请求标签
     *
     * @param url       请求的url
     * @param mapParams 请求的参数Json格式，注意json中如果存入float，需要转换成double,尽量不要用float，long用string代替
     * @param callback  回调接口
     * @param tag       网络请求标签
     */
    public void post(Context context, final String url, Map<String, String> mapParams, final IJSONCallback callback, Class<T> clazz,
                     String tag) {
        post(context, url, mapParams, callback, clazz, tag, true, false, true);
    }

    /**
     * post请求,不需要认证, 并设置网络请求标签
     *
     * @param url       请求的url
     * @param mapParams 请求的参数Json格式，注意json中如果存入float，需要转换成double,尽量不要用float，long用string代替
     * @param callback  回调接口
     * @param tag       网络请求标签
     */
    public void postNoAuth(Context context, final String url, Map<String, String> mapParams,
                           final IJSONCallback callback, Class<T> clazz,
                           String tag) {
        post(context, url, mapParams, callback, clazz, tag, true, false, false);
    }

    /**
     * post请求
     *
     * @param url           请求的url
     * @param mapParams     请求的参数Json格式，注意json中如果存入float，需要转换成double,尽量不要用float，long用string代替
     * @param callback      回调接口
     * @param tag           网络请求标签
     * @param isShowLoading 是否显示加载对话框
     * @param isToast       是否toast提示
     * @param isAuth        是否需要认证
     */
    public void post(final Context context, final String url, final Map<String, String> mapParams,
                     final IJSONCallback callback, final Class<T> clazz, String tag, final boolean isToast, final boolean isShowLoading, final boolean isAuth) {
        if (!isNetWorkAvailable(context, callback, isToast)) {
            return;
        }
//		LogUtil.i(TAG, "post url net normal: " + url + "| json: " + jsonStr);
        if (mapParams != null) {
            for (Map.Entry<String, String> entry : mapParams.entrySet()) {
//                LogUtil.i(TAG, "key:" + entry.getKey() + " | value: " + entry.getValue());
            }
        }
        try {

            Request<JSONObject> postRequest = new NormalPostRequest(url, new Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
//                    LogUtil.i(TAG, url + " sucess: " + response.toString());
//                    LogUtil.json(TAG, response.toString());
                    sucessCallBack(context, callback, clazz, isToast, response);
                }

            }, new ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
//                    LogUtil.i(TAG, url + " failture: ");
                    errorCallBack(context, callback, isToast, error);
                }
            }, mapParams) {
//                @Override
//                public Map<String, String> getHeaders() throws AuthFailureError {
//                    return getHttpHeader(context, isAuth, mapParams);
//                }
            };
            postRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            addToRequestQueue(context, postRequest, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取请求头
     *
     * @param isAuth 是否需要认证
     * @return
     */
    private HashMap<String, String> getHttpHeader(Context context, boolean isAuth, Map<String, String> params) {
//        HashMap<String, String> allMap = new HashMap<String, String>();
        HashMap<String, String> headers = new HashMap<String, String>();
//        String userToken = SkinSettingManager.getInstance(context).getSkinSetting().TOKEN.getValue();
//        if (!TextUtils.isEmpty(userToken)) {
//            headers.put("token", userToken);
//            allMap.put("token", userToken);
//
//        }
//        headers.put("version", AndroidUtil.getVersionName(context));
//        headers.put("mobile", "2");
//        headers.put("appsys", "1");
//        headers.put("channel", getMetaData(context, "UMENG_CHANNEL"));
//        headers.put("deviceno", GlobalUtil.getUUid(context));
//        headers.put("referer", HttpConstant.NEW_IP);
//        headers.put("phonemodel", android.os.Build.MODEL);
//        headers.put("phonesysversion", android.os.Build.VERSION.RELEASE);
//
//        allMap.put("version", AndroidUtil.getVersionName(context));
//        allMap.put("mobile", "2");
//        allMap.put("appsys", "1");
//        allMap.put("channel", getMetaData(context, "UMENG_CHANNEL"));
//        allMap.put("deviceno", GlobalUtil.getUUid(context));
//        allMap.put("referer", HttpConstant.NEW_IP);
//        allMap.put("phonemodel", android.os.Build.MODEL);
//        allMap.put("phonesysversion", android.os.Build.VERSION.RELEASE);
//
//        if (params != null) {
//            for (String key : params.keySet()) {
//                if (!TextUtils.isEmpty(params.get(key))) {
//                    allMap.put(key, params.get(key));
//                }
//            }
//        }
//
//        headers.put("sign", getSign(allMap));

        return headers;
    }

//    private String getMetaData(Context context, String key) {
//        String msg = "";
//        ApplicationInfo appInfo = null;
//        try {
//            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
//            msg = appInfo.metaData.getString(key);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        return msg;
//    }

//    private String getSign(Map<String, String> params) {
//
//        //这里将map.entrySet()转换成list
//        List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>(params.entrySet());
//        //然后通过比较器来实现排序
//        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
//            //升序排序
//            public int compare(Map.Entry<String, String> o1,
//                               Map.Entry<String, String> o2) {
//                return o1.getKey().compareTo(o2.getKey());
//            }
//
//        });
//
////        for (Map.Entry<String, String> mapping : list) {
////            LogUtil.i(TAG, mapping.getKey() + ":" + mapping.getValue());
////        }
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < list.size(); i++) {
//            sb.append(list.get(i).getKey());
//            sb.append('=');
//            sb.append(list.get(i).getValue());
//            if (i < list.size() - 1) {
//                sb.append('&');
//            }
//        }
////        LogUtil.i(TAG, "String :" + sb.toString());
//        String signStr = sb.toString();
//        signStr = signStr.replaceAll("[\\n\\r]", "");
//        String sign1 = ParseMD5.parseStrToMd5L32(signStr + "haopifu");
////        LogUtil.i(TAG, "String sign1:" + sign1);
////        LogUtil.i(TAG, "list.size : " + list.size());
//        int length = list.size() % 27 + 6;
////        LogUtil.i(TAG, "String length:" + length);
//        String signResult = sign1.substring(0, length);
////        LogUtil.i(TAG, "String result:" + signResult);
//        return signResult;
//    }


    /**
     * 获取请求队列
     *
     * @return
     */
    public RequestQueue getRequestQueue(Context context) {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context);
        }

        return mRequestQueue;
    }

    /**
     * 添加请求，并给请求添加标签
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Context context, Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue(context).add(req);
    }

    /**
     * 添加请求
     *
     * @param req
     */
    public <T> void addToRequestQueue(Context context, Request<T> req) {
        req.setTag(TAG);
        getRequestQueue(context).add(req);
    }

    /**
     * 取消请求 根据标签
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    /**
     * 继承JsonObjectRequest，用于utf-8转码
     */
    class MyJsonObjectRequest extends JsonObjectRequest {

        public MyJsonObjectRequest(int method, String url, JSONObject jsonRequest, Listener<JSONObject> listener,
                                   ErrorListener errorListener) {
            super(method, url, jsonRequest, listener, errorListener);
        }

        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data, "UTF-8");
                return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        }
    }

    class NormalPostRequest extends Request<JSONObject> {
        private Map<String, String> mMap;
        private Listener<JSONObject> mListener;

        public NormalPostRequest(String url, Listener<JSONObject> listener, ErrorListener errorListener, Map<String, String> map) {
            super(Method.POST, url, errorListener);

            mListener = listener;
            mMap = map;
        }

        //mMap是已经按照前面的方式,设置了参数的实例
        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            return mMap;
        }

        //此处因为response返回值需要json数据,和JsonObjectRequest类一样即可
        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data, "UTF-8");

                return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        }

        @Override
        protected void deliverResponse(JSONObject response) {
            mListener.onResponse(response);
        }
    }

}
