package com.segeuru.soft.monitering;

import android.app.Application;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MoniteringApp extends Application {

    private static float scale = 0;
    private static DBHelper m_dbHlper = null;

    @Override
    public void onCreate() {
        super.onCreate();
        MoniteringApp.scale = getResources().getDisplayMetrics().density;

        if(null == m_dbHlper) {
            m_dbHlper = new DBHelper(getApplicationContext(), "monitering.db", null, 4);
        }

//        SQLiteDatabase db = m_dbHlper.getWritableDatabase();
//        ContentValues cv = new ContentValues();
//        cv.put("name", "kim");
//        cv.put("value", "34");
//        long result = db.insert("sample", null, cv);
//        Log.e("db:", Long.toString(result));
//        db.close();

        Log.e("segeuru", "app start.");
    }

    public static int convToDP(int dp) {
        return (int)(dp * scale);
    }
    public static DBHelper dbHelper() { return m_dbHlper; }
}
