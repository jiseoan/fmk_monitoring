package com.segeuru.soft.monitering;

import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.widget.Toolbar;

public class WebviewActivity extends BaseAtivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setVisibility(View.GONE);

        initWebview(R.id.webview);
        m_webview.loadUrl("file:///android_asset/public/login.html");
    }

}

