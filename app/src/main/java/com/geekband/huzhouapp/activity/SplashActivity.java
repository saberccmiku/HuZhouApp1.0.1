package com.geekband.huzhouapp.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.database.dto.DataOperation;
import com.database.pojo.AppVersionTable;
import com.database.pojo.BaseTable;
import com.geekband.huzhouapp.R;
import com.geekband.huzhouapp.application.MyApplication;
import com.geekband.huzhouapp.utils.Constants;
import com.geekband.huzhouapp.utils.DataUtils;
import com.geekband.huzhouapp.utils.LinkNet;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/5/12
 */
public class SplashActivity extends Activity {

    private static final int NEED_UPDATE = 0;
    /**
     * 处理ui信息
     */
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case NEED_UPDATE:
                    BaseTable versionTable = (BaseTable) msg.obj;
                    showUpdateDialog(versionTable);
                    break;
                default:
                    break;
            }
            return false;
        }
    });


    private void showUpdateDialog(final BaseTable versionTable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("版本更新");
        builder.setMessage(versionTable.getField(AppVersionTable.FIELD_DETAIL));
        builder.setPositiveButton("欣然接受", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DataUtils.download(versionTable.getAccessaryFileUrlList().get(0), SplashActivity.this);
            }
        });

        builder.setNegativeButton("残忍拒绝", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //登陆
               toLogin();
            }
        });

        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 250) {
            if (resultCode == RESULT_CANCELED) {
                //登陆
               toLogin();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //加载网络数据,判断是否有网络
        if (LinkNet.isNetworkAvailable(this)) {

////            无服务器测试登录调试
//            startActivity(new Intent(SplashActivity.this, MainActivity.class));
//            SplashActivity.this.finish();
            initLoadingAnimation();
            checkUpdate();

        } else {
            String isAuto = MyApplication.sSharedPreferences.getString(Constants.AUTO_LOGIN, null);
            if (isAuto != null) {
                startActivity(new Intent(this, MainActivity.class));
                this.finish();
            } else {
                Toast.makeText(this, "当前网络不可用，部分功能无法运行", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 检测版本更新
     */
    private void checkUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //noinspection unchecked
                ArrayList<BaseTable> versionTables = (ArrayList<BaseTable>) DataOperation.queryTable(AppVersionTable.TABLE_NAME);
                if (versionTables != null && versionTables.size() != 0) {
                    int position = 0;
                    if (versionTables.size() > 1) {
                        for (int i = 0; i < versionTables.size() - 1; i++) {
                            String firstTime = versionTables.get(i).getField(AppVersionTable.FIELD_TIME);
                            long firstMillion = getMillionTime(firstTime);
                            String secondTime = versionTables.get(i + 1).getField(AppVersionTable.FIELD_TIME);
                            long secondMillion = getMillionTime(secondTime);
                            if (firstMillion > secondMillion) {
                                position = i;
                            } else {
                                position = i + 1;
                            }
                        }
                    } else {
                        position = 0;
                    }
                    BaseTable versionTable = versionTables.get(position);
                    String app_vn = versionTable.getField(AppVersionTable.FIELD_VN);
                    String currentVersionName = getVersionName();

                    if (!app_vn.equals(currentVersionName)) {
                        Message message = Message.obtain();
                        message.what = NEED_UPDATE;
                        message.obj = versionTable;
                        mHandler.sendMessage(message);
                    } else {
                        //没有最新版本，登陆
                        toLogin();
                    }
                } else {
                    //暂未发布版本，登陆
                    toLogin();
                }

            }
        }).start();

    }

    /**
     * 检测版本动画
     */
    private void initLoadingAnimation() {

        ImageView loading_animation = (ImageView) findViewById(R.id.loading_animation);
        ObjectAnimator animator = ObjectAnimator.ofFloat(loading_animation, "rotation", 0, 360);
        animator.setDuration(500);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setRepeatMode(ObjectAnimator.RESTART);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();

    }

    /**
     * 获取当前版本号
     *
     * @return
     */
    public String getVersionName() {
        //获取包管理器
        PackageManager pm = getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo(getPackageName(), PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            return packageInfo.versionName;
        }
        return null;
    }

    //登录
    public void toLogin() {

// 判断之前是否登录过，如果已经登录过就直接进入主界面，不再登录认证。如果退出登录过，那么进入登录界面登录进入

        String isAuto = MyApplication.sSharedPreferences.getString(Constants.AUTO_LOGIN, null);
        Intent intent = new Intent();
        if (isAuto != null) {
            intent.setClass(SplashActivity.this, MainActivity.class);
        } else {
            intent.setClass(SplashActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        SplashActivity.this.finish();

    }


    public void installApk(File apkFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        startActivityForResult(intent, 250);
    }

    public long getMillionTime(String time) {
        long timeMillion = 0;
        String subTime = time.substring(0, time.lastIndexOf("."));
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        try {
            timeMillion = sf.parse(subTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeMillion;
    }


}
