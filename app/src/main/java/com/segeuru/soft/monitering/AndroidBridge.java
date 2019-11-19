package com.segeuru.soft.monitering;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.JavascriptInterface;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Constraints;

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

        Log.d(DEBUG_TAG, sql);

        if(sql.substring(0, 6).toLowerCase().compareTo("select") == 0) {
            return m_webViewActivity.selectSQL(sql);
        }

        m_webViewActivity.m_db.execSQL(sql);
        return null;
    }

    @JavascriptInterface
    public String deviceUniq() {
        return Settings.Secure.getString(m_webView.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @JavascriptInterface
    public void showQrCameraBar(String str1, String str2) {
        m_webViewActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebView webView = m_webViewActivity.findViewById(R.id.webview);
                webView.getLayoutParams().height = 0;
                ConstraintSet constraintSet = new ConstraintSet();
                ConstraintLayout constraintLayout = m_webViewActivity.findViewById(R.id.constraint_layout_webActivity);
                constraintSet.clone(constraintLayout);
                constraintSet.connect(webView.getId(), ConstraintSet.BOTTOM, R.id.qr_camera_layout, ConstraintSet.TOP);
                constraintSet.applyTo(constraintLayout);

                LinearLayout linearLayout = m_webViewActivity.findViewById(R.id.qr_camera_layout);
                linearLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @JavascriptInterface
    public void showActionBar(String str1, String str2) {
        m_webViewActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebView webView = m_webViewActivity.findViewById(R.id.webview);
                webView.getLayoutParams().height = 0;
                ConstraintSet constraintSet = new ConstraintSet();
                ConstraintLayout constraintLayout = m_webViewActivity.findViewById(R.id.constraint_layout_webActivity);
                constraintSet.clone(constraintLayout);
                constraintSet.connect(webView.getId(), ConstraintSet.BOTTOM, R.id.actionBar_layout, ConstraintSet.TOP);
                constraintSet.applyTo(constraintLayout);

                LinearLayout linearLayout = m_webViewActivity.findViewById(R.id.actionBar_layout);
                linearLayout.setVisibility(View.VISIBLE);
            }
        });
    }

}
