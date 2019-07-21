package cn.hikyson.godeyedemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;


import cn.hikyson.android.godeye.toolbox.network.OkNetworkCollectorFactory;
import cn.hikyson.godeye.core.GodEye;
import cn.hikyson.godeye.core.internal.modules.network.Network;
import cn.hikyson.godeye.core.internal.modules.startup.Startup;
import cn.hikyson.godeye.core.internal.modules.startup.StartupInfo;
import cn.hikyson.godeye.monitor.GodEyeMonitor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends Activity {

    private static final int PORT = 5390;

    private OkHttpClient mOkHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final long homeCreateTime = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GodEyeMonitor.work(this);
        OkNetworkCollectorFactory factory = new OkNetworkCollectorFactory(GodEye.instance().<Network>getModule(GodEye.ModuleName.NETWORK));
        mOkHttpClient = new OkHttpClient.Builder().eventListenerFactory(factory).addNetworkInterceptor(factory.createInterceptor()).build();
        ((TextView) this.findViewById(R.id.address_tv)).setText(getAddressLog(this, PORT));
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        GodEye.instance().<Startup>getModule(GodEye.ModuleName.STARTUP).produce(new StartupInfo(MyApp.sApplicationStartTime > 0 ?
                                StartupInfo.StartUpType.COLD : StartupInfo.StartUpType.HOT, MyApp.sApplicationStartTime > 0 ? (System.currentTimeMillis() - MyApp.sApplicationStartTime) : (System.currentTimeMillis() - homeCreateTime)));
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
                            OkHttpClient client = mOkHttpClient;
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
        final long blockTime = Long.parseLong(String.valueOf(editText.getText()));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(blockTime);
                } catch (Throwable e) {
                }
            }
        });
    }

    public void jumpToLeak(View view) {
        Intent intent = new Intent(MainActivity.this, LeakActivity.class);
        startActivity(intent);
    }

    private static String getAddressLog(Context context, int port) {
        String condition = "Install Android Studio plugin [https://plugins.jetbrains.com/plugin/12114-androidgodeye] and connect android device to pc OR make sure android device and pc are in the same network segment";
        String openAddress = "AndroidGodEye dashboard is available in [http://localhost:" + port + "/index.html] or [" + getFormatIpAddress(context, port) + "].";
        return condition + "\n\n" + openAddress;
    }

    private static String getFormatIpAddress(Context context, int port) {
        @SuppressLint("WifiManagerPotentialLeak")
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager != null ? wifiManager.getConnectionInfo().getIpAddress() : 0;
        @SuppressLint("DefaultLocale") final String formattedIpAddress = String.format("%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
        return "http://" + formattedIpAddress + ":" + port + "/index.html";
    }

    public void viewHere(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://localhost:" + PORT + "/index.html"));
        startActivity(intent);
    }
}
