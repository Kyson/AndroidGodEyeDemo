package cn.hikyson.godeyedemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import cn.hikyson.android.godeye.toolbox.StartupTracer;
import cn.hikyson.godeye.core.GodEye;
import cn.hikyson.godeye.core.internal.modules.network.Network;
import cn.hikyson.godeye.core.internal.modules.network.RequestBaseInfo;
import cn.hikyson.godeye.core.internal.modules.startup.Startup;
import cn.hikyson.godeye.core.internal.modules.startup.StartupInfo;
import cn.hikyson.godeye.monitor.GodEyeMonitor;

public class MainActivity extends Activity {

    private static final int PORT = 5390;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final long homeCreateTime = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GodEyeMonitor.work(this);
        ((TextView) this.findViewById(R.id.address_tv)).setText(getAddressLog(this, PORT));

        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        Startup startup = GodEye.instance().getModule(Startup.class);
                        startup.produce(new StartupInfo(MyApp.sApplicationStartTime > 0 ?
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
                try {
                    long startTimeMillis = System.currentTimeMillis();
                    URL url = new URL("https://www.trip.com/");
                    //打开连接
                    HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                    if (200 == urlConnection.getResponseCode()) {
                        //得到输入流
                        InputStream is = urlConnection.getInputStream();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len = 0;
                        while (-1 != (len = is.read(buffer))) {
                            baos.write(buffer, 0, len);
                            baos.flush();
                        }
                        String result = baos.toString("utf-8");
                        long endTimeMillis = System.currentTimeMillis();
                        GodEye.instance().getModule(Network.class).produce(new RequestBaseInfo(startTimeMillis, endTimeMillis, result.getBytes().length, String.valueOf(url)));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        return "Open AndroidGodEye dashboard [ " + getFormatIpAddress(context, port) + " ] in your browser , if can not open it , make sure device and pc are on the same network segment";
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
        intent.setData(Uri.parse("http://localhost:5390/index.html"));
        startActivity(intent);
    }
}
