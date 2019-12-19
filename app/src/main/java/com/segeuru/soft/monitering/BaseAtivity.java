package com.segeuru.soft.monitering;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseAtivity extends AppCompatActivity {

    protected WebView m_webview = null;
    protected final Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //m_db.close();
    }

    protected void initWebview(int id) {
        m_webview = findViewById(id);
        m_webview.getSettings().setJavaScriptEnabled(true);
        m_webview.getSettings().setAllowFileAccessFromFileURLs(true);
        m_webview.getSettings().setAllowUniversalAccessFromFileURLs(true);
        m_webview.getSettings().setDomStorageEnabled(true);
        m_webview.setWebViewClient(new WebViewCustom());
    }

    protected SQLiteDatabase database() {
        return ((MoniteringApp)getApplication()).m_db;
    }

     protected String selectSQL(String sql) {
         Cursor cursor = database().rawQuery(sql, null);

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
