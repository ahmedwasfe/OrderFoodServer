package com.ahmet.orderfoodserver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ahmet.orderfoodserver.Common.Common;
import com.ahmet.orderfoodserver.Fragment.SignInFragment;
import com.google.android.gms.common.SignInButton;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Common.addFragment(new SignInFragment(), R.id.frame_layout_login, getSupportFragmentManager());
    }
}
