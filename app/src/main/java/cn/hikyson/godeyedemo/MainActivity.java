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

import cn.hikyson.godeye.core.GodEyeHelper;
import cn.hikyson.godeye.core.exceptions.UninstallException;
import cn.hikyson.godeye.core.internal.modules.startup.StartupInfo;
import cn.hikyson.godeye.monitor.GodEyeMonitor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xcrash.XCrash;

public class MainActivity extends Activity {

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GodEyeMonitor.shutDown();
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
        String openAddress = "AndroidGodEye dashboard is available on [http://localhost:5390/index.html], use plugin to open it.";
        String condition = "Install Android Studio plugin [https://plugins.jetbrains.com/plugin/12114-androidgodeye] to view details.";
        String logcat = "You can find the address in logcat by search 'AndroidGodEye monitor is running at port'.";
        return openAddress + "\n\n" + condition + "\n\n" + logcat;
    }

    private void openBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void makeCrash(View view) {
        XCrash.testJavaCrash(true);
    }
}
