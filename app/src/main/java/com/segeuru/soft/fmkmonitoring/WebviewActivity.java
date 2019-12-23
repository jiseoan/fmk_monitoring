package com.segeuru.soft.fmkmonitoring;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class WebviewActivity extends BaseAtivity {

    private static final int REQUEST_PERMISSION_CODE = 1000;
    private String[] PERMISSIONS = { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE };

    private final String DEBUG_TAG = "segeuru.com";
    public static final int REQUEST_CODE = 0x0000c0dd;
    private WebSupport m_webSupport;
    public String m_bottomBar_style;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);
        findViewById(R.id.btn_home).setOnClickListener(home_button_clickLisener);
        findViewById(R.id.btn_close).setOnClickListener(close_button_clickLisener);
        findViewById(R.id.btn_qr).setOnClickListener(qr_button_clickLisener);
        findViewById(R.id.btn_login).setOnClickListener(login_button_clickLisener);
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
        findViewById(R.id.btn_confirm_ext).setOnClickListener(confirm_button_clickLisener);
        findViewById(R.id.btn_complete).setOnClickListener(complete_button_clickLisener);
        findViewById(R.id.btn_cancel_complete).setOnClickListener(cancel_button_clickLisener);
        findViewById(R.id.btn_asProcessingRegistration).setOnClickListener(asProcessRegistration_button_clickLisener);
        findViewById(R.id.btn_list).setOnClickListener(list_button_clickLisener);
        hideAllActionBars();

        initWebview(R.id.webview);
        // 원격디버깅창을 쓰기위해 추가 - kim.js
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            m_webview.setWebContentsDebuggingEnabled(true);
        }
        m_webview.addJavascriptInterface(new AndroidBridge(this, m_webview), "android");
        m_webview.loadUrl("file:///android_asset/public/" + (getIntent().hasExtra("url") ? getIntent().getStringExtra("url") : "login.html"));
        m_webSupport = new WebSupport(this);

        //권한요구
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!hasPermissions(PERMISSIONS))
                requestPermissions(PERMISSIONS, REQUEST_PERMISSION_CODE);
        } else {
            //finish();
        }

        FirebaseInstanceId.getInstance().getInstanceId()
            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if (!task.isSuccessful()) {
                        Log.w(DEBUG_TAG, "getInstanceId failed", task.getException());
                        return;
                    }

                    // Get new Instance ID token
                    String token = task.getResult().getToken();
                    MoniteringApp.m_fbToken = token;
                }
            });
    }

    protected void javaScriptCallback(String command, String param, String result) {
        m_webview.loadUrl(String.format("Javascript:NativeCallback(\'%s\', \'%s\', \'%s\')", command, param, result));
    }

    protected void hideAllToolBars() {
        findViewById(R.id.main).setVisibility(View.GONE);
        findViewById(R.id.popup).setVisibility(View.GONE);
    }

    protected void hideAllBottomActionBars() {
        findViewById(R.id.login).setVisibility(View.GONE);
        findViewById(R.id.jobStart).setVisibility(View.GONE);
        findViewById(R.id.jobStartAndCancel).setVisibility(View.GONE);
        findViewById(R.id.retakeAndAsAndConfirm).setVisibility(View.GONE);
        findViewById(R.id.onlyList).setVisibility(View.GONE);
        findViewById(R.id.insertAndCancel).setVisibility(View.GONE);
        findViewById(R.id.reAsProcessing).setVisibility(View.GONE);
        findViewById(R.id.retakeAndConfirm).setVisibility(View.GONE);
        findViewById(R.id.asProcessing).setVisibility(View.GONE);
        findViewById(R.id.completeAndCancel).setVisibility(View.GONE);
        findViewById(R.id.confirm).setVisibility(View.GONE);
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
                    String result = data.getStringExtra("pics");
                    if(result.compareTo("[]") == 0) return; //canceled.

                    javaScriptCallback("cameraResult", data.getStringExtra("request_id"), result);
                }
            }
                break;
            case com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE: {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if(result.getContents().compareTo("cancel") == 0) return;

                javaScriptCallback("qrScanResult", "null", result.getContents());
//                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
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

    final View.OnClickListener login_button_clickLisener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            javaScriptCallback("login", "", "clicked");
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

    private boolean hasPermissions(String[] permissions) {
        int result;
        for(String perms : permissions) {
            result = ContextCompat.checkSelfPermission(this, perms);
            if(result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case REQUEST_PERMISSION_CODE:
                if(grantResults.length > 0) {
                    boolean result = true;
                    result &= grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    result &= grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(!result) {
                        Toast.makeText(this, "접근권한이 거부되었습니다.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                javaScriptCallback("back", "", "");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(m_webview.canGoBack()){
            m_webview.goBack();
        }else{
            super.onBackPressed();
        }
    }

    public WebSupport support() {
        return m_webSupport;
    }
}
