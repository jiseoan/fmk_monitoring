package com.segeuru.soft.monitering;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseAtivity extends AppCompatActivity {

    protected WebView m_webview = null;
    protected SQLiteDatabase m_db = null;
    protected final Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_db = MoniteringApp.dbHelper().getWritableDatabase();
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
        m_webview.setWebChromeClient(new WebChromeClient());
    }

    protected class AndroidBridge {

        @JavascriptInterface
        public String setMessage(final String msg) {


            JSONObject jsonObj = new JSONObject();

            JSONArray jArray = new JSONArray();
            JSONObject json = new JSONObject();
            try {
                json.put("aaa", "123");
                json.put("bbb", "333");
                jArray.put(json);

                json.put("aaa", "555");
                json.put("bbb", "666");
                jArray.put(json);

            } catch(Exception e) {

            }


//            Cursor cursor = m_db.rawQuery("select * from sample", null);
//
//            while (cursor.moveToNext()) {
//                for (int i = 0; i < cursor.getColumnCount(); ++i) {
//                    System.out.println(cursor.getColumnName(i));
//                    System.out.println(cursor.getString(i));
//                }
//                count++;
//            }
//
//            cursor.close();

            return jArray.toString();
        }
    }

}
