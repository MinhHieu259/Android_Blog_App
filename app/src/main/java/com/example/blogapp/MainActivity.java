package com.example.blogapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Tạm dừng app 1.5 giây
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
                boolean isLoggedIn = userPref.getBoolean("isLoggedIn", false);

                if (isLoggedIn){
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    finish();
                }else {
                    isFirstTime();
                }
            }
        }, 1500);
    }

    private void isFirstTime() {
        // kiểm tra chạy app lần đầu
        SharedPreferences preferences = getApplication().getSharedPreferences("onBoard", Context.MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean("isFirstTime", true);
        // set giá trị mặc định
        if (isFirstTime){
            SharedPreferences.Editor  editor = preferences.edit();
            editor.putBoolean("isFirstTime", false);
            editor.apply();

            // Bắt đầu Activity Onboard
            startActivity(new Intent(MainActivity.this, OnBoardActivity.class));
            finish();
        }else {
            // Bắt đầu Auth Activity
            startActivity(new Intent(MainActivity.this, AuthActivity.class));
            finish();
        }
    }
}