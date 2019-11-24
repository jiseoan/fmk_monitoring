package com.segeuru.soft.monitering;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.SQLException;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.JavascriptInterface;
import android.widget.LinearLayout;

import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONObject;

import androidx.appcompat.app.ActionBar;
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
    public void setTitle(String title) {
        final String actionBarTitle = title;
        m_webViewActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ActionBar actionBar = m_webViewActivity.getSupportActionBar();
                if(null != actionBar)
                    actionBar.setTitle(actionBarTitle);
            }
        });
    }

    @JavascriptInterface
    public void toolBar(String resourceName, final String title) {
        Resources res = null;

        try {
            Context resContext = m_webView.getContext().createPackageContext(m_webView.getContext().getPackageName(), 0);
            res = resContext.getResources();
        } catch (Exception e) {
            e.printStackTrace();
        }

        final int id = res.getIdentifier(resourceName, "id", m_webView.getContext().getPackageName());
        final boolean is_show = true;

        m_webViewActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_webViewActivity.hideAllToolBars();
                Toolbar toolbar = m_webViewActivity.findViewById(id);
                toolbar.setVisibility(is_show ? View.VISIBLE : View.GONE);
                m_webViewActivity.setSupportActionBar(is_show ? toolbar : null);
                m_webViewActivity.getSupportActionBar().setTitle(title);
            }
        });
    }

    @JavascriptInterface
    public void bottomActionBar(String resourceName, String style) {
        Resources res = null;

        try {
            Context resContext = m_webView.getContext().createPackageContext(m_webView.getContext().getPackageName(), 0);
            res = resContext.getResources();
        } catch (Exception e) {
            e.printStackTrace();
        }

        final int id = res.getIdentifier(resourceName, "id", m_webView.getContext().getPackageName());
        final boolean is_show = true;

        m_webViewActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_webViewActivity.hideAllBottomActionBars();
                WebView webView = m_webViewActivity.findViewById(R.id.webview);
                webView.getLayoutParams().height = 0;
                ConstraintSet constraintSet = new ConstraintSet();
                ConstraintLayout constraintLayout = m_webViewActivity.findViewById(R.id.constraint_layout_webActivity);
                constraintSet.clone(constraintLayout);
                constraintSet.connect(webView.getId(), ConstraintSet.BOTTOM, id, ConstraintSet.TOP);
                constraintSet.applyTo(constraintLayout);

                LinearLayout linearLayout = m_webViewActivity.findViewById(id);
                linearLayout.setVisibility(is_show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @JavascriptInterface
    public void hideAllActionBars() {
        m_webViewActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_webViewActivity.hideAllActionBars();
            }
        });
    }

    @JavascriptInterface
    public void qrCode() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(m_webViewActivity);
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setCaptureActivity(QrReaderActivity.class);
        intentIntegrator.initiateScan();
    }

    @JavascriptInterface
    public void Camera(String requestId, String jsonParams) {
//        Log.d(DEBUG_TAG, jsonParams);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonParams);
//            Log.d(DEBUG_TAG, jsonObject.getString("address"));
            Intent intent = new Intent(m_webViewActivity, CameraViewer.class);
            intent.putExtra("request_id", requestId);
            intent.putExtra("take_count", jsonObject.getInt("take_count"));
            intent.putExtra("title", jsonObject.getString("building_name"));
            intent.putExtra("address", jsonObject.getString("address"));
            m_webViewActivity.startActivityForResult(intent, WebviewActivity.REQUEST_CODE);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
