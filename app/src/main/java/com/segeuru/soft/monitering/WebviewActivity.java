package com.segeuru.soft.monitering;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;

public class WebviewActivity extends BaseAtivity {

    private LinearLayout m_bottom_layer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setVisibility(View.GONE);

        m_bottom_layer = findViewById(R.id.bottom_layer);
        //m_bottom_layer.setVisibility(View.GONE);

        initWebview(R.id.webview);
        m_webview.loadUrl(getIntent().getStringExtra("url"));
    }

    public void showBottomBar(boolean show) {
        m_bottom_layer.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
