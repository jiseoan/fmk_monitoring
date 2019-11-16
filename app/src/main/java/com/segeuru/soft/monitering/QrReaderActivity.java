package com.segeuru.soft.monitering;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class QrReaderActivity extends AppCompatActivity implements DecoratedBarcodeView.TorchListener {

    private CaptureManager m_captureManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_reader);

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
