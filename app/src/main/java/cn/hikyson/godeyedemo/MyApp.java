package cn.hikyson.godeyedemo;


import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.hikyson.godeye.core.GodEye;
import cn.hikyson.godeye.core.GodEyeConfig;
import cn.hikyson.godeye.monitor.GodEyeMonitor;
import cn.hikyson.godeye.monitor.modules.AppInfoLabel;

/**
 * Created by kysonchao on 2018/1/29.
 */
public class MyApp extends Application {
    public static long sApplicationStartTime;

    @Override
    public void onCreate() {
        super.onCreate();
        GodEye.instance().init(this);
        GodEyeMonitor.injectAppInfoConext(new GodEyeMonitor.AppInfoConext() {
            @Override
            public Context getContext() {
                return MyApp.this;
            }

            @Override
            public List<AppInfoLabel> getAppInfo() {
                List<AppInfoLabel> appInfoLabels = new ArrayList<>();
                appInfoLabels.add(new AppInfoLabel("ApplicationID:" + BuildConfig.APPLICATION_ID, ""));
                appInfoLabels.add(new AppInfoLabel("VersionName:" + BuildConfig.VERSION_NAME, ""));
                appInfoLabels.add(new AppInfoLabel("VersionCode:" + BuildConfig.VERSION_CODE, ""));
                appInfoLabels.add(new AppInfoLabel("BuildType:" + BuildConfig.BUILD_TYPE, ""));
                appInfoLabels.add(new AppInfoLabel("https://github.com/Kyson/AndroidGodEye", "https://github.com/Kyson/AndroidGodEye"));
                return appInfoLabels;
            }
        });
        GodEyeMonitor.setClassPrefixOfAppProcess(Collections.singletonList("cn.hikyson.godeyedemo"));
        if (isMainProcess(this)) {
            GodEye.instance().install(GodEyeConfig.fromAssets("android-godeye-config/install.config"));
        }
        sApplicationStartTime = System.currentTimeMillis();
    }

    /**
     * 获取当前进程名
     */
    private static boolean isMainProcess(Application application) {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) application.getSystemService
                (Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
            if (process.pid == pid) {
                processName = process.processName;
            }
        }
        return application.getPackageName().equals(processName);
    }
}
