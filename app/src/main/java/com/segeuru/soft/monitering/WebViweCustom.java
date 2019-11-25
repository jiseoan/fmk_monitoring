package com.segeuru.soft.monitering;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViweCustom extends WebViewClient {

    private final String DEBUG_TAG = "segeuru.com";

//    @Override
//    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//        Log.i(DEBUG_TAG, "shouldOverrideUrlLoading");
//        String mode = request.getUrl().getQueryParameter("mode");
//        if(mode != null) {
//            if(mode.compareTo("activity") == 0) {
//                Intent intent = new Intent(view.getContext(), WebviewActivity.class);
//                intent.putExtra("url", request.getUrl().toString());
//                view.getContext().startActivity(intent);
//                return true;
//            }
//        }
//
//        return super.shouldOverrideUrlLoading(view, request);
//    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

}
