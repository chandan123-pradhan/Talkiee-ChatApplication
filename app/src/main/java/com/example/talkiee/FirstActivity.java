package com.example.talkiee;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class FirstActivity extends AppCompatActivity {
    private int i=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);


        startMainActivity();

    }

    private void startMainActivity() {


        Handler mHander=new Handler();
        mHander.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent=new Intent(FirstActivity.this,MainActivity.class);
                startActivity(mainIntent);
                finish();


            }
        },2000L);







    }


}
