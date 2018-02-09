package cn.hikyson.godeyedemo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.v4.util.ArrayMap;

import java.util.Map;

import cn.hikyson.android.godeye.toolbox.crash.CrashFileProvider;
import cn.hikyson.android.godeye.toolbox.rxpermission.RxPermissions;
import cn.hikyson.godeye.core.GodEye;
import cn.hikyson.godeye.core.helper.PermissionRequest;
import cn.hikyson.godeye.core.internal.modules.battery.Battery;
import cn.hikyson.godeye.core.internal.modules.battery.BatteryContextImpl;
import cn.hikyson.godeye.core.internal.modules.cpu.Cpu;
import cn.hikyson.godeye.core.internal.modules.cpu.CpuContextImpl;
import cn.hikyson.godeye.core.internal.modules.crash.Crash;
import cn.hikyson.godeye.core.internal.modules.fps.Fps;
import cn.hikyson.godeye.core.internal.modules.fps.FpsContextImpl;
import cn.hikyson.godeye.core.internal.modules.leakdetector.LeakContextImpl2;
import cn.hikyson.godeye.core.internal.modules.leakdetector.LeakDetector;
import cn.hikyson.godeye.core.internal.modules.memory.Heap;
import cn.hikyson.godeye.core.internal.modules.memory.Pss;
import cn.hikyson.godeye.core.internal.modules.memory.PssContextImpl;
import cn.hikyson.godeye.core.internal.modules.memory.Ram;
import cn.hikyson.godeye.core.internal.modules.memory.RamContextImpl;
import cn.hikyson.godeye.core.internal.modules.pageload.Pageload;
import cn.hikyson.godeye.core.internal.modules.pageload.PageloadContextImpl;
import cn.hikyson.godeye.core.internal.modules.sm.Sm;
import cn.hikyson.godeye.core.internal.modules.sm.SmContextImpl;
import cn.hikyson.godeye.core.internal.modules.thread.ThreadContextImpl;
import cn.hikyson.godeye.core.internal.modules.thread.ThreadDump;
import cn.hikyson.godeye.core.internal.modules.thread.deadlock.DeadLock;
import cn.hikyson.godeye.core.internal.modules.thread.deadlock.DeadLockContextImpl;
import cn.hikyson.godeye.core.internal.modules.thread.deadlock.DeadlockDefaultThreadFilter;
import cn.hikyson.godeye.core.internal.modules.traffic.Traffic;
import cn.hikyson.godeye.core.internal.modules.traffic.TrafficContextImpl;
import cn.hikyson.godeye.monitor.GodEyeMonitor;
import io.reactivex.Observable;

/**
 * Created by kysonchao on 2018/1/29.
 */
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
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
        GodEye.instance().install(Cpu.class, new CpuContextImpl())
                .install(Battery.class, new BatteryContextImpl(this))
                .install(Fps.class, new FpsContextImpl(this))
                .install(Heap.class, Long.valueOf(2000))
                .install(Pss.class, new PssContextImpl(this))
                .install(Ram.class, new RamContextImpl(this))
                .install(Sm.class, new SmContextImpl(this, 1000, 300, 800))
                .install(Traffic.class, new TrafficContextImpl())
                .install(Crash.class, new CrashFileProvider(this))
                .install(ThreadDump.class, new ThreadContextImpl())
                .install(DeadLock.class, new DeadLockContextImpl(GodEye.instance().getModule(ThreadDump.class).subject(), new DeadlockDefaultThreadFilter()))
                .install(Pageload.class, new PageloadContextImpl(this))
                .install(LeakDetector.class, new LeakContextImpl2(this, new PermissionRequest() {
                    @Override
                    public Observable<Boolean> dispatchRequest(Activity activity, String... permissions) {
                        return new RxPermissions(activity).request(permissions);
                    }
                }));
    }
}
