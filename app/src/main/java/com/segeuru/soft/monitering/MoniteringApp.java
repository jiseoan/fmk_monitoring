package com.segeuru.soft.monitering;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class MoniteringApp extends Application {

    private static float scale = 0;
    private static DBHelper m_dbHlper = null;
    public static final String APP_STORE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/fmk";

    @Override
    public void onCreate() {
        super.onCreate();
        MoniteringApp.scale = getResources().getDisplayMetrics().density;

        if(null == m_dbHlper) {
            m_dbHlper = new DBHelper(getApplicationContext(), "monitering.db", null, 5);
        }

        //create folder.
        File dir = new File(MoniteringApp.APP_STORE_PATH);
        if(!dir.exists()) dir.mkdirs();

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
