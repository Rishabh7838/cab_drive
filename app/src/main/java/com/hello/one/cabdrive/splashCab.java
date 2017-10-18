package com.hello.one.cabdrive;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class splashCab extends AppCompatActivity {
    private static  final int splash_Tm = 2000;
    private Handler mHandler;
    private Runnable mRunnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_cab);
        mHandler = new Handler();
        Thread splasThread = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(4000);
                    Intent i = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(i);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        splasThread.start();

    }
}
