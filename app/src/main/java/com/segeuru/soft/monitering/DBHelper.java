package com.segeuru.soft.monitering;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table test (id integer primary key autoincrement, name text, value text)");
        db.execSQL("CREATE TABLE IF NOT EXISTS agent ( agent_code TEXT NOT NULL PRIMARY KEY, agent_name TEXT, agent_type TEXT, job_area TEXT, job_week TEXT, mobile_code TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists test");
        onCreate(db);
    }

}
