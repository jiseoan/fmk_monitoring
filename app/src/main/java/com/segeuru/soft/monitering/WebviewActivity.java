package com.segeuru.soft.monitering;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.annotation.Nullable;

public class WebviewActivity extends BaseAtivity {

    private String DEBUG_TAG = "segeuru.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);
        findViewById(R.id.btn_home).setOnClickListener(home_button_clickLisener);
        findViewById(R.id.btn_close).setOnClickListener(close_button_clickLisener);
        findViewById(R.id.btn_qr).setOnClickListener(qr_button_clickLisener);
        hideAllActionBars();

        initWebview(R.id.webview);
        m_webview.addJavascriptInterface(new AndroidBridge(this, m_webview), "android");
        m_webview.loadUrl(getIntent().getStringExtra("url"));
    }

    protected void javaScriptCallback(String command, String param, String result) {
        m_webview.loadUrl(String.format("Javascript:NativeCallback(\"%s\", \"%s\", \"%s\")", command, param, result));
    }

    protected void hideAllToolBars() {
        findViewById(R.id.main).setVisibility(View.GONE);
        findViewById(R.id.popup).setVisibility(View.GONE);
    }

    protected void hideAllBottomActionBars() {
        findViewById(R.id.jobStart).setVisibility(View.GONE);
        findViewById(R.id.jobStartAndCancel).setVisibility(View.GONE);
        findViewById(R.id.reTakeAndAsAndConfirm).setVisibility(View.GONE);
        findViewById(R.id.list).setVisibility(View.GONE);
        findViewById(R.id.insertAndCancel).setVisibility(View.GONE);
        findViewById(R.id.reAsProcessing).setVisibility(View.GONE);
        findViewById(R.id.reTakeAndAndConfirm).setVisibility(View.GONE);
        findViewById(R.id.asProcessing).setVisibility(View.GONE);
        findViewById(R.id.completeAndCancel).setVisibility(View.GONE);
    }

    protected void hideAllActionBars() {
        hideAllToolBars();
        hideAllBottomActionBars();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        javaScriptCallback("qrResult", "null", result.getContents());
        Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
    }

    final View.OnClickListener home_button_clickLisener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            javaScriptCallback("home", "", "");
        }
    };

    final View.OnClickListener close_button_clickLisener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            javaScriptCallback("close", "", "");
        }
    };

    final View.OnClickListener qr_button_clickLisener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            javaScriptCallback("jobStart", "", "job.start.clicked");
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                javaScriptCallback("back", "", "");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
