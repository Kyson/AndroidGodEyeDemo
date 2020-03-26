package cn.hikyson.godeyedemo;


import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.hikyson.android.godeye.okhttp.GodEyePluginOkNetwork;
import cn.hikyson.godeye.core.GodEyeHelper;
import cn.hikyson.godeye.core.monitor.AppInfoConext;
import cn.hikyson.godeye.core.monitor.AppInfoLabel;
import okhttp3.OkHttpClient;

/**
 * Created by kysonchao on 2018/1/29.
 */
public class MyApp extends Application {
    public static long sApplicationStartTime;

    @Override
    public void onCreate() {
        super.onCreate();
        GodEyeHelper.setMonitorAppInfoConext(new AppInfoConext() {
            @Override
            public List<cn.hikyson.godeye.core.monitor.AppInfoLabel> getAppInfo() {
                List<AppInfoLabel> appInfoLabels = new ArrayList<>();
                appInfoLabels.add(new AppInfoLabel("ApplicationID", BuildConfig.APPLICATION_ID, null));
                appInfoLabels.add(new AppInfoLabel("VersionName", BuildConfig.VERSION_NAME, ""));
                appInfoLabels.add(new AppInfoLabel("VersionCode", String.valueOf(BuildConfig.VERSION_CODE), ""));
                appInfoLabels.add(new AppInfoLabel("BuildType", BuildConfig.BUILD_TYPE, ""));
                appInfoLabels.add(new AppInfoLabel("AndroidGodEye", "https://github.com/Kyson/AndroidGodEye", "https://github.com/Kyson/AndroidGodEye"));
                return appInfoLabels;
            }
        });
        sApplicationStartTime = System.currentTimeMillis();
        MyIntentService.startActionBaz(this, "", "");
    }

    private static OkHttpClient sOkHttpClient;

    public static OkHttpClient getOkHttpClientInstance() {
        if (sOkHttpClient == null) {
            GodEyePluginOkNetwork godEyePluginOkNetwork = new GodEyePluginOkNetwork();
            sOkHttpClient = new OkHttpClient.Builder().eventListenerFactory(godEyePluginOkNetwork).addNetworkInterceptor(godEyePluginOkNetwork).build();
        }
        return sOkHttpClient;
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
            Log.d("kyson", "process.processName:" + process.processName);
            Log.d("kyson", "process.pid:" + process.pid);
            Log.d("kyson", "process.uid:" + process.uid);

            if (process.pid == pid) {
                processName = process.processName;
            }
        }
        return application.getPackageName().equals(processName);
    }
}
