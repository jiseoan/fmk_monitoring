package com.segeuru.soft.monitering;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;

public class WebviewActivity extends BaseAtivity {

    private String DEBUG_TAG = "segeuru.com";
    protected LinearLayout m_qr_camera_layer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setVisibility(View.GONE);

        m_qr_camera_layer = findViewById(R.id.qr_camera_layout);
        m_qr_camera_layer.setVisibility(View.GONE);

        findViewById(R.id.actionBar_layout).setVisibility(View.GONE);

        initWebview(R.id.webview);
        m_webview.addJavascriptInterface(new AndroidBridge(this, m_webview), "android");
        m_webview.loadUrl(getIntent().getStringExtra("url"));
    }

    public void showBottomBar(boolean show) {
        m_qr_camera_layer.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
