package com.segeuru.soft.monitering;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

        Log.d(DEBUG_TAG, "onCreate");

        setContentView(R.layout.activity_webview);
        findViewById(R.id.top_actionbar_A1).setVisibility(View.GONE);
        findViewById(R.id.top_actionbar_A2).setVisibility(View.GONE);

        findViewById(R.id.btn_qr).setOnClickListener(qr_button_clickLisener);


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

    protected void JavaScriptCallback(String command, String param, String result) {
        m_webview.loadUrl(String.format("Javascript:NativeCallback(\"%s\", \"%s\", \"%s\")", command, param, result));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        JavaScriptCallback("qr.result", "null", result.getContents());
        Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
    }

    final View.OnClickListener qr_button_clickLisener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IntentIntegrator intentIntegrator = new IntentIntegrator(WebviewActivity.this);
            intentIntegrator.setBeepEnabled(true);
            intentIntegrator.setCaptureActivity(QrReaderActivity.class);
            intentIntegrator.initiateScan();
        }
    };

}
