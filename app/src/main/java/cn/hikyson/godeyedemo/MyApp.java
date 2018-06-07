package cn.hikyson.godeyedemo;


import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.support.v4.util.ArrayMap;

import java.util.Map;

import cn.hikyson.android.godeye.toolbox.crash.CrashFileProvider;
import cn.hikyson.android.godeye.toolbox.rxpermission.RxPermissionRequest;
import cn.hikyson.godeye.core.GodEye;
import cn.hikyson.godeye.core.installconfig.BatteryConfig;
import cn.hikyson.godeye.core.installconfig.CpuConfig;
import cn.hikyson.godeye.core.installconfig.CrashConfig;
import cn.hikyson.godeye.core.installconfig.FpsConfig;
import cn.hikyson.godeye.core.installconfig.HeapConfig;
import cn.hikyson.godeye.core.installconfig.LeakConfig;
import cn.hikyson.godeye.core.installconfig.PageloadConfig;
import cn.hikyson.godeye.core.installconfig.PssConfig;
import cn.hikyson.godeye.core.installconfig.RamConfig;
import cn.hikyson.godeye.core.installconfig.SmConfig;
import cn.hikyson.godeye.core.installconfig.ThreadConfig;
import cn.hikyson.godeye.core.installconfig.TrafficConfig;
import cn.hikyson.godeye.monitor.GodEyeMonitor;

/**
 * Created by kysonchao on 2018/1/29.
 */
public class MyApp extends Application {
    public static long sApplicationStartTime;

    @Override
    public void onCreate() {
        super.onCreate();
        if (isMainProcess(this)) {
            GodEye.instance().init(this);
            GodEyeMonitor.injectAppInfoConext(new GodEyeMonitor.AppInfoConext() {
                @Override
                public Context getContext() {
                    return MyApp.this;
                }

                @Override
                public Map<String, Object> getAppInfo() {
                    Map<String, Object> appInfo = new ArrayMap<>();
                    appInfo.put("ApplicationID", BuildConfig.APPLICATION_ID);
                    appInfo.put("VersionName", BuildConfig.VERSION_NAME);
                    appInfo.put("VersionCode", BuildConfig.VERSION_CODE);
                    appInfo.put("BuildType", BuildConfig.BUILD_TYPE);
                    return appInfo;
                }
            });
            GodEye.instance()
                    .install(new BatteryConfig(this))
                    .install(new CpuConfig())
                    .install(new CrashConfig(new CrashFileProvider(this)))
                    .install(new FpsConfig(this))
                    .install(new HeapConfig())
                    .install(new LeakConfig(this, new RxPermissionRequest()))
                    .install(new PageloadConfig(this))
                    .install(new PssConfig(this))
                    .install(new RamConfig(this))
                    .install(new SmConfig(this))
                    .install(new ThreadConfig())
                    .install(new TrafficConfig());
            sApplicationStartTime = System.currentTimeMillis();
        }
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
