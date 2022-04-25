package com.mrctf.android2022;

import static androidx.fake.stub.e.fs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fake.stub.e;


public class StubApp extends Activity {
    private static ClassLoader classLoader;
    private static int flag = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(flag == 1){
            try {
                Class<?> clz = classLoader.loadClass("com.mrctf.android2022.MainActivity");
                startActivity(new Intent(getApplicationContext(), clz));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this,"您当前环境不安全，请更换设备",Toast.LENGTH_LONG).show();
            this.finish();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }
            }, 1000);

        }

    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        if (e.checkInit(context)){
            classLoader = e.init();
            if(classLoader != null) flag = 1;
            else flag = 0;
        }else{
            flag = 0;
        }

    }
}