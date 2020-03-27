package cn.hikyson.godeyedemo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import cn.hikyson.godeye.core.GodEye;
import cn.hikyson.godeye.core.GodEyeHelper;
import cn.hikyson.godeye.core.exceptions.UninstallException;
import cn.hikyson.godeye.core.internal.modules.crash.CrashInfo;
import cn.hikyson.godeye.core.internal.modules.network.NetworkInfo;
import cn.hikyson.godeye.core.internal.modules.sm.BlockInfo;
import cn.hikyson.godeye.core.internal.modules.startup.StartupInfo;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xcrash.XCrash;

public class MainActivity extends Activity {
    CompositeDisposable mCompositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final long homeCreateTime = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((TextView) this.findViewById(R.id.note)).setText(getNote());
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            GodEyeHelper.onAppStartEnd(new StartupInfo(MyApp.sApplicationStartTime > 0 ?
                                    StartupInfo.StartUpType.COLD : StartupInfo.StartUpType.HOT, MyApp.sApplicationStartTime > 0 ? (System.currentTimeMillis() - MyApp.sApplicationStartTime) : (System.currentTimeMillis() - homeCreateTime)));
                        } catch (UninstallException e) {
                            e.printStackTrace();
                        }
                        MyApp.sApplicationStartTime = 0;
                    }
                });
            }
        });
        observeWhenRelease();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupWhenRlease();
    }

    public void request(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OkHttpClient client = MyApp.getOkHttpClientInstance();
                            Request request = new Request.Builder()
                                    .url("https://tech.hikyson.cn/")
                                    .build();
                            Response response = client.newCall(request).execute();
                            String body = response.body().string();
                            Log.d("androidgodeye", "request result:" + response.code());
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("androidgodeye", "request result:" + String.valueOf(e));
                        }
                    }
                }).start();
            }
        }).start();
    }

    public void block(View view) {
        EditText editText = findViewById(R.id.block_et);
        try {
            final long blockTime = Long.parseLong(String.valueOf(editText.getText()));
            runOnUiThread(() -> {
                try {
                    Thread.sleep(blockTime);
                } catch (Throwable e) {
                }
            });
        } catch (Throwable e) {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Input valid time for jank(block)!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    public void jumpToLeak(View view) {
        Intent intent = new Intent(MainActivity.this, LeakActivity.class);
        startActivity(intent);
    }

    private static String getNote() {
        if ("debug".equalsIgnoreCase(BuildConfig.BUILD_TYPE)) {
            String note1 = "This is a debug apk. Debug monitor is opened. Notification config is opened.";
            String openAddress = "AndroidGodEye dashboard is available on [http://localhost:5390/index.html], use plugin to open it.";
            String condition = "Install Android Studio plugin(Named 'AndroidGodEye') [https://plugins.jetbrains.com/plugin/12114-androidgodeye] to view details.";
            String logcat = "You can find the address in logcat by search 'AndroidGodEye monitor is running at port'.";
            return openAddress + "\n\n" + condition + "\n\n" + logcat;
        } else {
            String note1 = "This is a release apk. Debug monitor is closed. Notification config is closed.";
            return note1;
        }
    }

    public void makeCrash(View view) {
        XCrash.testJavaCrash(true);
    }

    private void observeWhenRelease() {
        if (!"debug".equalsIgnoreCase(BuildConfig.BUILD_TYPE)) {
            mCompositeDisposable = new CompositeDisposable();
            try {
                mCompositeDisposable.add(GodEye.instance().observeModule(GodEye.ModuleName.NETWORK, new Consumer<NetworkInfo>() {
                    @Override
                    public void accept(NetworkInfo networkInfo) throws Exception {
                        AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "This is NetworkInfo message from release:" + networkInfo.summary, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }));
            } catch (UninstallException e) {
                e.printStackTrace();
            }
            try {
                mCompositeDisposable.add(GodEye.instance().observeModule(GodEye.ModuleName.SM, new Consumer<BlockInfo>() {
                    @Override
                    public void accept(BlockInfo blockInfo) throws Exception {
                        AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "This is BlockInfo message from release:" + blockInfo.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }));
            } catch (UninstallException e) {
                e.printStackTrace();
            }
            try {
                mCompositeDisposable.add(GodEye.instance().observeModule(GodEye.ModuleName.CRASH, new Consumer<List<CrashInfo>>() {
                    @Override
                    public void accept(List<CrashInfo> crashInfos) throws Exception {
                        AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "This is CrashInfo message from release:" + crashInfos.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }));
            } catch (UninstallException e) {
                e.printStackTrace();
            }
        }
    }

    private void cleanupWhenRlease() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.dispose();
        }
    }
}
