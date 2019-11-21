package com.segeuru.soft.monitering;

import android.content.Context;
import android.content.res.Resources;
import android.database.SQLException;
import android.provider.Settings;
import android.view.View;
import android.webkit.WebView;
import android.webkit.JavascriptInterface;
import android.widget.LinearLayout;

import com.google.zxing.integration.android.IntentIntegrator;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class AndroidBridge {

    private final String DEBUG_TAG = "segeuru.com";
    private WebviewActivity m_webViewActivity;
    private WebView m_webView;

    public AndroidBridge(WebviewActivity activity, WebView webview) {
        m_webViewActivity = activity;
        m_webView = webview;
    }

    @JavascriptInterface
    public String DBSQL(final String sql) {

        if(sql.substring(0, 6).toLowerCase().compareTo("select") == 0) {
            return m_webViewActivity.selectSQL(sql);
        }

        try {
            m_webViewActivity.m_db.execSQL(sql);
        } catch (SQLException e) {
            return "failed";
        }
        return "success";
    }

    @JavascriptInterface
    public String deviceUniq() {
        return Settings.Secure.getString(m_webView.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @JavascriptInterface
    public void toolBar(String resourceName, boolean show) {
        Resources res = null;

        try {
            Context resContext = m_webView.getContext().createPackageContext(m_webView.getContext().getPackageName(), 0);
            res = resContext.getResources();
        } catch (Exception e) {
            e.printStackTrace();
        }

        final int id = res.getIdentifier(resourceName, "id", m_webView.getContext().getPackageName());

        if(show) {
            m_webViewActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toolbar toolbar = m_webViewActivity.findViewById(id);
                    toolbar.setVisibility(View.VISIBLE);
                    m_webViewActivity.setSupportActionBar(toolbar);
                }
            });
        } else {
            m_webViewActivity.findViewById(id).setVisibility(View.GONE);
            m_webViewActivity.setSupportActionBar(null);
        }
    }

    @JavascriptInterface
    public void bottomActionBar(String resourceName, boolean show) {
        Resources res = null;

        try {
            Context resContext = m_webView.getContext().createPackageContext(m_webView.getContext().getPackageName(), 0);
            res = resContext.getResources();
        } catch (Exception e) {
            e.printStackTrace();
        }

        final int id = res.getIdentifier(resourceName, "id", m_webView.getContext().getPackageName());

        if(show) {
            m_webViewActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WebView webView = m_webViewActivity.findViewById(R.id.webview);
                    webView.getLayoutParams().height = 0;
                    ConstraintSet constraintSet = new ConstraintSet();
                    ConstraintLayout constraintLayout = m_webViewActivity.findViewById(R.id.constraint_layout_webActivity);
                    constraintSet.clone(constraintLayout);
                    constraintSet.connect(webView.getId(), ConstraintSet.BOTTOM, id, ConstraintSet.TOP);
                    constraintSet.applyTo(constraintLayout);

                    LinearLayout linearLayout = m_webViewActivity.findViewById(id);
                    linearLayout.setVisibility(View.VISIBLE);
                }
            });
        } else {
            LinearLayout linearLayout = m_webViewActivity.findViewById(id);
            linearLayout.setVisibility(View.GONE);
        }
    }

    @JavascriptInterface
    public void runQrCamera() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(m_webViewActivity);
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setCaptureActivity(QrReaderActivity.class);
        intentIntegrator.initiateScan();
    }

}
