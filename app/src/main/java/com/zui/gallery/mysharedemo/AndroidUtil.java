package com.zui.gallery.mysharedemo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

/**
 * Created by zcf2015 on 15/5/28. 封装安卓自带方法工具类
 */
public class AndroidUtil {

    /**
     * 显示键盘
     *
     * @param context 环境上下文
     * @param view    显示键盘的view, 一般是EditText
     * @since 显示软键盘
     */
    public static void showInputMethod(Activity context, View view) {
        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(view, 0);
    }

    /**
     * 设置软键盘隐藏
     *
     * @param context  环境上下文
     * @param editText 输入框
     */
    public static void setInputmethodHide(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        // 隐藏软键盘
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    // /**
    // * 设置软键盘隐藏
    // *
    // * @param context 环境上下文
    // * @param editText 输入框
    // */
    // public static void setInputmethodHide(Context context) {
    // InputMethodManager imm = (InputMethodManager)
    // context.getSystemService(Activity.INPUT_METHOD_SERVICE);
    // imm.toggleSoftInput(
    // InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    // }

    /**
     * 将dp转换成px
     *
     * @param context
     * @param dpValue
     * @return 将dp转换成px
     */
    public static int dip2px(Context context, int dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * @return 设备版本号
     */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取版本号名称
     *
     * @return 当前应用的版本号
     */
    public static String getVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static int getVersionCode(Context context) {
        PackageManager manager = context.getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取包名
     *
     * @return 当前应用的包名
     */
    public static String getPackageName(Context context) {
        PackageManager manager = context.getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 得到设备屏幕的宽度
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 得到设备屏幕的高度
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 得到设备的密度
     */
    public static float getScreenDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    /**
     * 判断是wifi还是3g网络
     * @param context
     * @return
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkINfo = cm.getActiveNetworkInfo();
        if (networkINfo != null
                && networkINfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * @param context
     * @return 返回网络是否可用
     */
    public static boolean isNetworkAvailable(Context context) {
        return isNetworkAvailable(context, true);
    }

    /**
     * @param context
     * @param isToast 是否提示toast
     * @return 返回网络是否可用
     */
    public static boolean isNetworkAvailable(Context context, boolean isToast) {
        if (context == null)
            return false;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isAvailable();
        }
        if (isToast) {
            Toast.makeText(context, context.getResources().getString(R.string.network_unavailable), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public static String getCurrentNetType(Context context) {
        String type = "";
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            type = "";
        } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            type = "WIFI";
        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int subType = info.getSubtype();
            if (subType == TelephonyManager.NETWORK_TYPE_CDMA || subType == TelephonyManager.NETWORK_TYPE_GPRS
                    || subType == TelephonyManager.NETWORK_TYPE_EDGE) {
                type = "2G";
            } else if (subType == TelephonyManager.NETWORK_TYPE_UMTS || subType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_A
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_B) {
                type = "3G";
            } else if (subType == TelephonyManager.NETWORK_TYPE_LTE) {// LTE是3g到4g的过渡，是3.9G的全球标准
                type = "4G";
            } else if (subType == TelephonyManager.NETWORK_TYPE_UNKNOWN) {// 未知
                type = "OTHER";
            }
        }
        return type;
    }

    /**
     * 是否在前台运行
     *
     * @param context
     * @return
     */
    public static boolean isRunningForeground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        return currentPackageName != null && currentPackageName.equals(context.getPackageName());
    }

    /**
     * 获取指定dip值
     */
    public static int obtainDipValue(Context context, int des) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                des, metrics);
    }

    /**
     * Returns a SystemProperty
     *
     * @param propName The Property to retrieve
     * @return The Property, or NULL if not found
     */
    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        return line;
    }

    /**
     * 判断 当前系统是否是小米，并且是miui6以上
     *
     * @return
     */
    public static boolean getSystemIsMiUi() {
        return Build.MANUFACTURER.equalsIgnoreCase("Xiaomi") && !"V5".equals(getSystemProperty("ro.miui.ui.version.name "));
    }

    /**
     * 关闭软键盘
     */
    public static void setHideSoftKeyboard(Activity context) {
        try {
            if (context != null) {
                InputMethodManager inputManager = (InputMethodManager) context
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputManager.isActive()) {
                    inputManager.hideSoftInputFromWindow(context.getCurrentFocus()
                            .getWindowToken(), 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取本地资源Uri
     *
     * @param activity
     * @param resId
     * @return
     */

    public static Uri getLoacalResUri(Activity activity, int resId) {
        return Uri.parse("res://" + activity.getPackageName() + "/" + resId);
    }

    /**
     * 获取当前进程名
     *
     * @param context
     * @return 进程名
     */
    public static final String getProcessName(Context context) {
        String processName = null;

        // ActivityManager
        ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));

        while (true) {
            for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
                if (info.pid == android.os.Process.myPid()) {
                    processName = info.processName;
                    break;
                }
            }

            // go home
            if (!TextUtils.isEmpty(processName)) {
                return processName;
            }

            // take a rest and again
            try {
                Thread.sleep(100L);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    public static String formetFileSize(long fileS) {
        if(fileS == 0){
            return "0";
        }
        DecimalFormat df = new DecimalFormat("#.0");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

}
