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
    public static final int REQUEST_CODE = 0x0000c0dd;
    public String m_bottomBar_style;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);
        findViewById(R.id.btn_home).setOnClickListener(home_button_clickLisener);
        findViewById(R.id.btn_close).setOnClickListener(close_button_clickLisener);
        findViewById(R.id.btn_qr).setOnClickListener(qr_button_clickLisener);
        findViewById(R.id.btn_retakePicture).setOnClickListener(retake_button_clickLisener);
        findViewById(R.id.btn_request_as).setOnClickListener(requestAs_button_clickLisener);
        findViewById(R.id.btn_confirm).setOnClickListener(confirm_button_clickLisener);
        findViewById(R.id.btn_monitoringCamera).setOnClickListener(monitoringCamera_button_clickLisener);
        findViewById(R.id.btn_cancel).setOnClickListener(cancel_button_clickLisener);
        findViewById(R.id.btn_registration).setOnClickListener(registration_button_clickLisener);
        findViewById(R.id.btn_cancel_registration).setOnClickListener(cancel_button_clickLisener);
        findViewById(R.id.btn_rebuildingCamera).setOnClickListener(rebuildingCamera_button_clickLisener);
        findViewById(R.id.btn_confirm_rebuilding).setOnClickListener(confirm_button_clickLisener);
        findViewById(R.id.btn_asProcessingReRegistration).setOnClickListener(reregistrationAs_button_clickLisener);
        findViewById(R.id.btn_complete).setOnClickListener(complete_button_clickLisener);
        findViewById(R.id.btn_cancel_complete).setOnClickListener(cancel_button_clickLisener);
        findViewById(R.id.btn_asProcessingRegistration).setOnClickListener(asProcessRegistration_button_clickLisener);
        findViewById(R.id.btn_list).setOnClickListener(list_button_clickLisener);
        hideAllActionBars();

        initWebview(R.id.webview);
        m_webview.addJavascriptInterface(new AndroidBridge(this, m_webview), "android");
        m_webview.loadUrl("file:///android_asset/public/" + getIntent().getStringExtra("url"));
    }

    protected void javaScriptCallback(String command, String param, String result) {
        m_webview.loadUrl(String.format("Javascript:NativeCallback(\'%s\', \'%s\', \'%s\')", command, param, result));
    }

    protected void hideAllToolBars() {
        findViewById(R.id.main).setVisibility(View.GONE);
        findViewById(R.id.popup).setVisibility(View.GONE);
    }

    protected void hideAllBottomActionBars() {
        findViewById(R.id.jobStart).setVisibility(View.GONE);
        findViewById(R.id.jobStartAndCancel).setVisibility(View.GONE);
        findViewById(R.id.retakeAndAsAndConfirm).setVisibility(View.GONE);
        findViewById(R.id.onlyList).setVisibility(View.GONE);
        findViewById(R.id.insertAndCancel).setVisibility(View.GONE);
        findViewById(R.id.reAsProcessing).setVisibility(View.GONE);
        findViewById(R.id.retakeAndConfirm).setVisibility(View.GONE);
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

        switch (requestCode) {
            case WebviewActivity.REQUEST_CODE: {
                if(null != data) {
                    Log.i(DEBUG_TAG, data.getStringExtra("pics"));
                    String result = data.getStringExtra("pics");
                    javaScriptCallback("cameraResult", data.getStringExtra("request_id"), result);
                }
            }
                break;
            case com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE: {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                javaScriptCallback("qrScanResult", "null", result.getContents());
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
                break;
        }

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
            javaScriptCallback("qrScan", "", "clicked");
        }
    };

    final View.OnClickListener retake_button_clickLisener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            javaScriptCallback("reMonitoringCamera", "", "clicked");
        }
    };

    final View.OnClickListener requestAs_button_clickLisener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            javaScriptCallback("asRequest", "", "clicked");
        }
    };

    final View.OnClickListener confirm_button_clickLisener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            javaScriptCallback("confirm", "", "clicked");
        }
    };

    final View.OnClickListener monitoringCamera_button_clickLisener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            javaScriptCallback("monitoringCamera", "", "clicked");
        }
    };

    final View.OnClickListener cancel_button_clickLisener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            javaScriptCallback("cancel", "", "clicked");
        }
    };

    final View.OnClickListener registration_button_clickLisener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            javaScriptCallback("registration", "", "clicked");
        }
    };

    final View.OnClickListener rebuildingCamera_button_clickLisener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(m_bottomBar_style.toLowerCase().compareTo("ad") == 0) {
                javaScriptCallback("reAdCamera", "", "clicked");
                return;
            }

            javaScriptCallback("reBuildingCamera", "", "clicked");
        }
    };

    final View.OnClickListener cancel_rebuilding_button_clickLisener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            javaScriptCallback("cancel", "", "clicked");
        }
    };

    final View.OnClickListener reregistrationAs_button_clickLisener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            javaScriptCallback("asProcessingReRegistration", "", "clicked");
        }
    };

    final View.OnClickListener complete_button_clickLisener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            javaScriptCallback("complete", "", "clicked");
        }
    };

    final View.OnClickListener asProcessRegistration_button_clickLisener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            javaScriptCallback("asProcessingRegistration", "", "clicked");
        }
    };

    final View.OnClickListener list_button_clickLisener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            javaScriptCallback("list", "", "clicked");
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
