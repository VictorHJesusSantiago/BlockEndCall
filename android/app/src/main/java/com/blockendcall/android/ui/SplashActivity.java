package com.blockendcall.android.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.blockendcall.android.util.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(this);
        Intent intent = session.isLoggedIn()
                ? new Intent(this, MainActivity.class)
                : new Intent(this, LoginActivity.class);

        startActivity(intent);
        finish();
    }
}
