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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        findViewById(R.id.top_actionbar_A1).setVisibility(View.GONE);
        findViewById(R.id.top_actionbar_A2).setVisibility(View.GONE);
        //setSupportActionBar(m_toolBar);

        findViewById(R.id.bottom_actionbar_A1).setVisibility(View.GONE);
        findViewById(R.id.bottom_actionbar_A2).setVisibility(View.GONE);
        findViewById(R.id.bottom_actionbar_A3).setVisibility(View.GONE);
        findViewById(R.id.bottom_actionbar_B1).setVisibility(View.GONE);
        findViewById(R.id.bottom_actionbar_B2).setVisibility(View.GONE);
        findViewById(R.id.bottom_actionbar_C1).setVisibility(View.GONE);
        findViewById(R.id.bottom_actionbar_C2).setVisibility(View.GONE);
        findViewById(R.id.bottom_actionbar_D1).setVisibility(View.GONE);
        findViewById(R.id.bottom_actionbar_D2).setVisibility(View.GONE);

        initWebview(R.id.webview);
        m_webview.addJavascriptInterface(new AndroidBridge(this, m_webview), "android");
        m_webview.loadUrl(getIntent().getStringExtra("url"));
    }

//    public void showBottomBar(boolean show) {
//        m_qr_camera_layer.setVisibility(show ? View.VISIBLE : View.GONE);
//    }
}
