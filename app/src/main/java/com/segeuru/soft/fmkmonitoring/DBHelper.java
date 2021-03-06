package com.segeuru.soft.fmkmonitoring;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table test (id integer primary key autoincrement, name text, value text)");
        
        db.execSQL("CREATE TABLE IF NOT EXISTS agent ( agent_code TEXT NOT NULL PRIMARY KEY, agent_name TEXT, mobile TEXT, agent_type TEXT, job_area TEXT, job_week TEXT, mobile_code TEXT)");
        
        db.execSQL("CREATE TABLE IF NOT EXISTS notice ( notice_id INTEGER NOT NULL PRIMARY KEY, notice_type TEXT, title TEXT, content TEXT, create_date TEXT)");
        
        db.execSQL("CREATE TABLE IF NOT EXISTS building ( building_id INTEGER NOT NULL PRIMARY KEY, building_name TEXT, address TEXT, machine_cnt INTEGER)");
        
        db.execSQL("CREATE TABLE IF NOT EXISTS building_locate ( building_locate_id INTEGER NOT NULL PRIMARY KEY, building_id INTEGER, dong TEXT, unit_name TEXT, machine_code TEXT, qr_serial_code TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS temp_save_building_qrcode ( building_locate_id INTEGER NOT NULL PRIMARY KEY, qr_serial_code TEXT, upload_json TEXT)");
        
        db.execSQL("CREATE TABLE IF NOT EXISTS monitoring_request ( monitoring_request_id INTEGER NOT NULL PRIMARY KEY, building_id INTEGER, machine_cnt INTEGER, building_locate_ids TEXT, request_date TEXT, processing_flag TEXT, processing_date TEXT )");
        
        db.execSQL("CREATE TABLE IF NOT EXISTS ad_check_request ( ad_check_request_id INTEGER NOT NULL, ad_name TEXT, ad_type TEXT, ad_url TEXT, request_date TEXT, ad_check_building_id INTEGER, building_id INTEGER, building_file_url TEXT, processing_flag TEXT, return_code_id INTEGER, return_desc TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS temp_save_ad_check_building ( assignment_agent_code TEXT, building_id INTEGER, due_date TEXT, upload_json TEXT)");
        
        db.execSQL("CREATE TABLE IF NOT EXISTS processing ( building_id INTEGER, building_locate_id INTEGER, machine_code TEXT, processing_file_url TEXT, processing_date TEXT, qr_flag TEXT, no_qr_type_code_id INTEGER, no_qr_desc TEXT, monitoring_request_id INTEGER, ad_check_building_id INTEGER, processing_id INTEGER)");
        //db.execSQL("CREATE TABLE IF NOT EXISTS temp_save_processing ( processing_agent_code TEXT, building_id INTEGER, building_locate_id INTEGER, dong TEXT, unit_name TEXT, machine_code TEXT, processing_file_url TEXT, processing_date TEXT, qr_flag TEXT, no_qr_type_code_id INTEGER, no_qr_desc TEXT, monitoring_request_id INTEGER, ad_check_building_id INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS temp_save_processing ( processing_agent_code TEXT, building_id INTEGER, building_locate_id INTEGER, monitoring_request_id INTEGER, ad_check_building_id INTEGER, upload_json TEXT)");
        
        db.execSQL("CREATE TABLE IF NOT EXISTS code ( code_id INTEGER, parent_id INTEGER, code TEXT, codename TEXT)");
        
        db.execSQL("CREATE TABLE IF NOT EXISTS as_request ( building_id INTEGER, building_locate_id INTEGER, machine_code TEXT, request_date TEXT, request_type_code_ids TEXT, request_desc TEXT, request_file_url TEXT, processing_flag TEXT, as_request_id INTEGER)");
        //db.execSQL("CREATE TABLE IF NOT EXISTS temp_save_as_request ( building_id INTEGER, building_name TEXT, address TEXT, building_locate_id INTEGER, dong TEXT, unit_name TEXT, machine_code TEXT, request_date TEXT, request_type_code_ids TEXT, request_desc TEXT, request_file_url TEXT, request_name TEXT, request_mobile TEXT, request_agent_code TEXT)");
        
        db.execSQL("CREATE TABLE IF NOT EXISTS as_processing ( as_request_id INTEGER, processing_type_code_id INTEGER, processing_desc TEXT, processing_date TEXT, as_processing_id INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS temp_save_as_processing ( as_request_id INTEGER, processing_type_code_id INTEGER, processing_desc TEXT, processing_date TEXT, as_processing_id INTEGER)");
        
        db.execSQL("CREATE TABLE IF NOT EXISTS version ( ver_date TEXT, agent_code TEXT, notice INTEGER, building INTEGER, monitoring_request INTEGER, ad_check_request INTEGER, processing INTEGER, code INTEGER, as_request INTEGER, as_processing INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists test");
        onCreate(db);
    }

}
