package com.segeuru.soft.monitering;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static com.segeuru.soft.monitering.MoniteringApp.dbHelper;

public class BaseAtivity extends AppCompatActivity {

    protected WebView m_webview = null;
    protected SQLiteDatabase m_db = null;
    protected final Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_db = dbHelper().getWritableDatabase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_db.close();
    }

    protected void initWebview(int id) {
        m_webview = findViewById(id);
        m_webview.addJavascriptInterface(new AndroidBridge(), "android");
        m_webview.getSettings().setJavaScriptEnabled(true);
        m_webview.getSettings().setAllowFileAccessFromFileURLs(true);
        m_webview.getSettings().setAllowUniversalAccessFromFileURLs(true);
        m_webview.getSettings().setDomStorageEnabled(true);
        //m_webview.setWebChromeClient(new WebChromeClient());
        m_webview.setWebViewClient(new WebViweCustom());
    }

    protected class AndroidBridge {

        @JavascriptInterface
        public String DBSQL(final String sql) {

            if(sql.substring(0, 6).compareTo("select") == 0) {
                return selectSQL(sql);
            }

            m_db.execSQL(sql);
            return null;
        }
    }

     protected String selectSQL(String sql) {
         Cursor cursor = m_db.rawQuery(sql, null);

         JSONArray jsonArray = new JSONArray();
         while (cursor.moveToNext()) {
             JSONObject json = new JSONObject();
             for (int i = 0; i < cursor.getColumnCount(); i++) {
                 try {
                     switch (cursor.getType(i)) {
                         case Cursor.FIELD_TYPE_NULL:
                             break;
                         case Cursor.FIELD_TYPE_BLOB:
                             json.put(cursor.getColumnName(i), cursor.getBlob(i));
                             break;
                         case Cursor.FIELD_TYPE_INTEGER:
                             json.put(cursor.getColumnName(i), cursor.getInt(i));
                             break;
                         case Cursor.FIELD_TYPE_FLOAT:
                             json.put(cursor.getColumnName(i), cursor.getFloat(i));
                             break;
                         case Cursor.FIELD_TYPE_STRING:
                             json.put(cursor.getColumnName(i), cursor.getString(i));
                             break;
                     }

                     json.put(cursor.getColumnName(i), cursor.getString(i));
                 } catch(Exception e) {

                 }
             }
             jsonArray.put(json);
         }

         cursor.close();
         return jsonArray.toString();
     }
}
