package cn.hikyson.godeyedemo;


import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import cn.hikyson.android.godeye.toolbox.network.GodEyePluginOkNetwork;
import cn.hikyson.godeye.core.GodEye;
import cn.hikyson.godeye.core.GodEyeConfig;
import cn.hikyson.godeye.core.utils.ProcessUtils;
import cn.hikyson.godeye.monitor.GodEyeMonitor;
import okhttp3.OkHttpClient;

/**
 * Created by kysonchao on 2018/1/29.
 */
public class MyApp extends Application {
    public static long sApplicationStartTime;

    @Override
    public void onCreate() {
        super.onCreate();
        GodEye.instance().init(this);
        if (ProcessUtils.isMainProcess(this)) {//install in main process
            GodEye.instance().install(GodEyeConfig.fromAssets("android-godeye-config/install.config"));
        }
        GodEyeMonitor.work(this);

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
