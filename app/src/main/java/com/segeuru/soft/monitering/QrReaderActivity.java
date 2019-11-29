package com.segeuru.soft.monitering;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class QrReaderActivity extends AppCompatActivity implements DecoratedBarcodeView.TorchListener {

    private CaptureManager m_captureManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_reader);

        findViewById(R.id.btn_skip_qr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(QrReaderActivity.this, WebviewActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                //startActivityForResult(intent, REQUEST_CODE);
                Intent intent = new Intent();
                intent.putExtra(Intents.Scan.RESULT, "http://fmkr.kr/event/?cd=T00001");
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        DecoratedBarcodeView decoratedBarcodeView = findViewById(R.id.qr_reader_view);
        decoratedBarcodeView.setTorchListener(this);

        m_captureManager = new CaptureManager(this, decoratedBarcodeView);
        m_captureManager.initializeFromIntent(getIntent(), savedInstanceState);
        m_captureManager.decode();
    }

    @Override
    public void onTorchOn() {

    }

    @Override
    public void onTorchOff() {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        m_captureManager.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        m_captureManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        m_captureManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_captureManager.onDestroy();
    }
}
