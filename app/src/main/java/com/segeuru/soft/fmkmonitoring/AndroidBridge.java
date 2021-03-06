package com.segeuru.soft.fmkmonitoring;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.JavascriptInterface;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class AndroidBridge {

    private final String DEBUG_TAG = "segeuru.com";
    private final Handler handler = new Handler();
    private WebviewActivity m_webViewActivity;
    private WebView m_webView;
    private TextView m_currentActionBarTextView = null;

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
            SQLiteDatabase db = MoniteringApp.dbHelper().getReadableDatabase();
            db.execSQL(sql);
            db.close();
        } catch (SQLException e) {
            return "failed";
        }
        return "success";
    }

    @JavascriptInterface
    public String deviceUniq() {
        //return Settings.Secure.getString(m_webView.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.i(DEBUG_TAG, MoniteringApp.m_fbToken);
        return MoniteringApp.m_fbToken;
    }

    @JavascriptInterface
    public void setTitle(String title) {
        final String actionBarTitle = title;
        m_webViewActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                ActionBar actionBar = m_webViewActivity.getSupportActionBar();
//                if(null != actionBar)
//                    actionBar.setTitle(actionBarTitle);
                if(null != m_currentActionBarTextView)
                    m_currentActionBarTextView.setText(actionBarTitle);
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

        if(null == m_webViewActivity.findViewById(id)) {
            Log.i("chromium", String.format("%s 아이디를 찾을 수 없습니다.", resourceName));
            return;
        }
        m_webViewActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_webViewActivity.hideAllToolBars();
                Toolbar toolbar = m_webViewActivity.findViewById(id);
                toolbar.setVisibility(is_show ? View.VISIBLE : View.GONE);
                m_webViewActivity.setSupportActionBar(is_show ? toolbar : null);
                m_webViewActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                ArrayList<TextView> children = getChildren(toolbar, TextView.class);
                if(children.size() == 0) {
                    Log.i(DEBUG_TAG, "not found child " + toolbar);
                    return;
                }

                m_currentActionBarTextView = children.get(0);
                m_currentActionBarTextView.setText(title);
            }
        });
    }

    @JavascriptInterface
    public void bottomActionBar(String resourceName, final String style) {

        Resources res = null;

        try {
            Context resContext = m_webView.getContext().createPackageContext(m_webView.getContext().getPackageName(), 0);
            res = resContext.getResources();
        } catch (Exception e) {
            e.printStackTrace();
        }

        final int id = res.getIdentifier(resourceName, "id", m_webView.getContext().getPackageName());
        final boolean is_show = true;

        if(null == m_webViewActivity.findViewById(id)) {
            Log.i("chromium", String.format("%s 아이디를 찾을 수 없습니다.", resourceName));
            return;
        }
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
                m_webViewActivity.m_bottomBar_style = style;
            }
        });
    }

    @JavascriptInterface
    public void hideBottomActionBar() {
        m_webViewActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_webViewActivity.hideAllBottomActionBars();
            }
        });
    }

    @JavascriptInterface
    public void hideToolBar() {
        m_webViewActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_webViewActivity.hideAllToolBars();
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
        intentIntegrator.setPrompt("");
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setCaptureActivity(QrReaderActivity.class);
        intentIntegrator.initiateScan();
    }

    @JavascriptInterface
    public void camera(String requestId, String frontMessage, String jsonParams) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonParams);
            Intent intent = new Intent(m_webViewActivity, CameraViewer.class);
            intent.putExtra("request_id", requestId);
            intent.putExtra("frontMessage", frontMessage);
            intent.putExtra("take_count", jsonObject.optInt("take_count", 1));
            intent.putExtra("title", jsonObject.optString("title"));
            intent.putExtra("address", jsonObject.optString("address"));
            m_webViewActivity.startActivityForResult(intent, WebviewActivity.REQUEST_CODE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void newWebView(String url) {
        Intent intent = new Intent(m_webViewActivity, WebviewActivity.class);
        intent.putExtra("url", url);
        //m_webViewActivity.startActivity(intent);
        m_webViewActivity.startActivityForResult(intent,0);
    }

    @JavascriptInterface
    public void closeWebView() {
        //현재 웹뷰 닫기
//        Intent intent = new Intent();
//        intent.putExtra("result", m_webViewActivity.m_webViewResult);
//        m_webViewActivity.setResult(0, intent);
//        m_webViewActivity.finish();
        m_webViewActivity.close();
    }

    @JavascriptInterface
    public void setWebViewResult(String result) {
        m_webViewActivity.m_webViewResult = result;
    }

    @JavascriptInterface
    public void loadingProgress(boolean show) {

        final boolean is_show = show;
        m_webViewActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_webViewActivity.findViewById(R.id.loadingProgress).setVisibility(is_show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @JavascriptInterface
    public void deleteFiles(String jsonFiles) {
        try {
            Log.i(DEBUG_TAG, jsonFiles);
            JSONArray files = new JSONArray(jsonFiles);
            for(int i=0;i<files.length();++i) {
                JSONObject file = (JSONObject)files.get(i);
                File fileToDelete = new File(MoniteringApp.APP_STORE_PICTURE_PATH, file.getString("filename"));
                if(fileToDelete.exists()) fileToDelete.delete();
                Log.i(DEBUG_TAG, file.getString("filename"));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void deleteFilesAll() {
        try {
            //delete directory
            File directory = new File(MoniteringApp.APP_STORE_PICTURE_PATH);
            FileUtils.deleteDirectory(directory);

            //make directory
            File dirPicture = new File(MoniteringApp.APP_STORE_PICTURE_PATH);
            dirPicture.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void downloadMedia(String title, String mimeType, String url) {
        Intent intent = new Intent(m_webViewActivity, DownloadActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("mime_type", mimeType);
        intent.putExtra("url", url);
        m_webViewActivity.startActivity(intent);
    }

    @JavascriptInterface
    public void uploadData(String json) {
        m_webViewActivity.support().uploadData(json);
    }

    @JavascriptInterface
    public int getCheckNetwork() {
        ConnectivityManager manager = (ConnectivityManager)m_webViewActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if(networkInfo != null) {
            int type = networkInfo.getType();
            if (type == ConnectivityManager.TYPE_MOBILE) return 1; //mobile.
            else if (type == ConnectivityManager.TYPE_WIFI) return 2; //wifi.
            }
        return 0; //no connected.
    }

    @JavascriptInterface
    public void massQueries(String jsonString) {
        Log.i(DEBUG_TAG, jsonString);
        final String jsonstr = jsonString;

        handler.post(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = MoniteringApp.dbHelper().getReadableDatabase();
                db.beginTransaction();
                try {
                    JSONArray queries = new JSONArray(jsonstr);
                    Log.i(DEBUG_TAG, "processing database queries " + Integer.toString(queries.length()));
                    for(int i=0;i<queries.length();++i) {
                        JSONObject query = (JSONObject)queries.get(i);
                        db.execSQL(query.getString("query"));
                        //Log.i(DEBUG_TAG, query.getString("query"));
                        //if((i % 1000) == 0) Log.i(DEBUG_TAG, "processed 1000 queries");
                    }
                    db.setTransactionSuccessful();
                } catch(Exception e) {
                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                    db.close();
                }
                Log.i(DEBUG_TAG, "ended processing.");
                m_webViewActivity.javaScriptCallback("completeQueries", "", "");
            }
        });
    }

    @JavascriptInterface
    public void toastMessage(String message) {
        Toast toast =  Toast.makeText(m_webViewActivity, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private static <T extends View> ArrayList<T> getChildren(ViewGroup parent, Class<T> clazz)
    {
        ArrayList<T> children = new ArrayList<>();
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            View child = parent.getChildAt(i);
            if (child instanceof ViewGroup) {
                children.addAll(getChildren((ViewGroup)child, clazz));
                if(children.size() > 0) return children;
            }

            if(clazz.isInstance(child))
                children.add((T) child);
        }

        return children;
    }

}
