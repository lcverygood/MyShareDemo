package com.zui.gallery.mysharedemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.share.WbShareHandler;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnShare;
    private SsoHandler mSsoHandler;
    private Oauth2AccessToken mAccessToken;
    private Button mBtnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnShare = (Button) findViewById(R.id.btn_share);
        mBtnRegister = (Button) findViewById(R.id.btn_register);
        mBtnShare.setOnClickListener(this);
        mBtnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_share:
                shareWb();
                break;
            case R.id.btn_register:
                sSo();
                registerAppToWeibo();
                break;
        }
    }

    private void shareWb() {
        requestInit();
    }

    private void requestInit() {
        if (isNetConnection(getApplicationContext())) {
            requestWeiBo();
        } else if (!isWeiBoInstalled(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "请安装新浪微博客户端", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), "当前无网络连接，请检查网络", Toast.LENGTH_SHORT).show();
    }
    public static final String DCIM =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
    private void requestWeiBo() {
        String md5 = DCIM+"/testimage.jpg";
        md5 = getMD5(md5);
        Map<String, String> mapParams = new HashMap<>();
        mapParams.put("source", Constants.APP_KEY);
        mapParams.put("type", "panorama_image");
        mapParams.put("name", "testimage");
        mapParams.put("length", md5.getBytes().length+"");
        mapParams.put("check", md5);
        mapParams.put("client", "android");
        if(mAccessToken != null){
            mapParams.put("access_token", mAccessToken.toString());//mAccessToken
        }
//        mapParams.put("access_token", mAccessToken.toString());
        //下面不是必填项
//        map.put("callback","");
//        map.put("status_text","");
//        map.put("status_visible","");
//        map.put("mediaprops","");

        HttpManager.getInstance().post(getApplicationContext(),
                "http://multimedia.api.weibo.com/2/multimedia/open_init.json",
                mapParams,
                new IJSONCallback() {
                    @Override
                    public void onSuccess(Object json) {
                        ShareBean shareBean = (ShareBean) json;
                        if (!shareBean.getError().isEmpty()) {
                            Toast.makeText(getApplicationContext(),
                                    "init接口访问失败，失败信息为：" + shareBean.getError(), Toast.LENGTH_SHORT).show();
                        } else if (!shareBean.getFileToken().isEmpty()) {
                            getFileToken(shareBean.getFileToken());
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String desc) {

                        Toast.makeText(getApplicationContext(),
                                "errorCode为：" + errorCode + "///" + "desc为：" + desc, Toast.LENGTH_SHORT).show();
                    }
                }
                , ShareBean.class);
    }

    private void getFileToken(String fileToken) {

        Map<String, String> mapParams = new HashMap<>();
        mapParams.put("source", "");
        mapParams.put("filetoken", fileToken);
        mapParams.put("sectioncheck", "");
        mapParams.put("startloc", "");
        mapParams.put("client", "android");
        mapParams.put("type", "panorama_image");//
        HttpManager.getInstance().post(getApplicationContext(),
                "http://multimedia.api.weibo.com/2/multimedia/open_upload.json", mapParams, new IJSONCallback() {

                    @Override
                    public void onSuccess(Object json) {

                        UploadBean uploadBean = (UploadBean) json;
                        if (!uploadBean.getError().isEmpty()) {
                            Toast.makeText(getApplicationContext(),
                                    "upload接口访问失败，失败信息为：" + uploadBean.getError(), Toast.LENGTH_SHORT).show();
                        } else if (!uploadBean.getFid().isEmpty()) {
                            uploadSuccessOver(uploadBean.getFid());
                        } else if (!uploadBean.getSucc().isEmpty()) {
                            uploadSuccess(uploadBean.getSucc());
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String desc) {

                    }
                }, UploadBean.class);
    }

    private void uploadSuccessOver(String fid) {

        Toast.makeText(getApplicationContext(),
                "分享完毕，分享fid为：" + fid, Toast.LENGTH_SHORT).show();
    }

    private void uploadSuccess(String succ) {
        Toast.makeText(getApplicationContext(),
                "已" + succ + "分享出一个分片：", Toast.LENGTH_SHORT).show();
    }

    private String getMD5(String md5) {
        File file = null;
        try {
            String fileName = Environment.getExternalStorageDirectory().
                    getAbsoluteFile() + File.separator + "DCIM/Camera/1.jpg";
            if (file == null) {
                file = new File(fileName);
            }
            FileInputStream fis = new FileInputStream(file);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = fis.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, length);
            }
            BigInteger bigInt = new BigInteger(1, md.digest());
            System.out.println("文件md5值：" + bigInt.toString(16));
            md5 = bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5;
    }

    private void registerAppToWeibo() {
        WbShareHandler shareHandler = new WbShareHandler(this);
        shareHandler.registerApp();
    }

    private void sSo() {
        WbSdk.install(this, new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE));
        mSsoHandler = new SsoHandler(this);
        mSsoHandler.authorizeClientSso(new SelfWbAuthListener());
    }


    private class SelfWbAuthListener implements com.sina.weibo.sdk.auth.WbAuthListener {
        @Override
        public void onSuccess(final Oauth2AccessToken token) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAccessToken = token;
                    if (mAccessToken.isSessionValid()) {
                        // 保存 Token 到 SharedPreferences
                        AccessTokenKeeper.writeAccessToken(getApplicationContext(), mAccessToken);
                        Toast.makeText(getApplicationContext(),
                                "授权成功", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void cancel() {
            Toast.makeText(getApplicationContext(),
                    "取消授权", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFailure(WbConnectErrorMessage errorMessage) {
            Toast.makeText(getApplicationContext(), errorMessage.getErrorMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 判断网络是否连接及根据连接方式进行相应操作
     *
     * @author liuchao23 2017/8/29 16:25
     */
    private boolean isNetConnection(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                //TODO 当前网络状况wifi连接，可以进行操作
                return true;
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                //TODO dialog提醒用户，是否继续在连接网络但是非WIFI环境下操作
                showRemindDialog();
            }
        }
        return false;
    }

    private void showRemindDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("提示");
        builder.setMessage("当前为移动数据连接,您是否切换到WIFI环境下?");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "请您切换到WIFI环境", Toast.LENGTH_SHORT).show();
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        builder.setNeutralButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "有钱任性，继续使用移动数据", Toast.LENGTH_SHORT).show();
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * 是否安装新浪微博
     *
     * @author liuchao23 2017/8/29 13:58
     */
    private boolean isWeiBoInstalled(Context context) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            return false;
        }
        List<PackageInfo> listInfo = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : listInfo) {
            String strPackageInfo = packageInfo.packageName.toLowerCase(Locale.ENGLISH);
            if ("com.sina.weibo".equals(strPackageInfo)) {
                return true;
            }
        }
        return false;
    }
}
