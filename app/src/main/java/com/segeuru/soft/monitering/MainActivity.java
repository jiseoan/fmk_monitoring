package com.segeuru.soft.monitering;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private DBHelper mDBHlper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.btn_webview);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, WebviewActivity.class);
//                startActivity(intent);


                if(null == mDBHlper) {
                    mDBHlper = new DBHelper(MainActivity.this, "test.db", null, 1);
                }

                SQLiteDatabase db = mDBHlper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put("name", "kim");
                cv.put("value", "34");
                long result = db.insert("sample", null, cv);
                Log.e("db:", Long.toString(result));
                mDBHlper.close();
            }
        });
    }
}
