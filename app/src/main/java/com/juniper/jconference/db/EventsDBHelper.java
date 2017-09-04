package com.juniper.jconference.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by koteswara on 7/7/17.
 */

public class EventsDBHelper extends SQLiteOpenHelper{

    //DB NAMES AND VERSION
    public static final String DATABASE_NAME="jcallevent.db";
    public static final int VERSION=1;

    //TABLE NAME
    public static final String EVENTS_TABLE="events";
    public static final String CURRENT_EVENTS_TABLE="currentevents";

    public static final String KEY_DEBUG=null;

    //COLOUMN NAMES TABLE EVENTS_DETAILS
    public static final String KEY_ID="id";
    public static final String KEY_TIME="time";
    public static final String KEY_DATE="date";
    public static final String KEY_EVENT="event";
    public static final String KEY_CONFIG_ID="configid";
    public static final String KEY_TELEPHONE_NUMBERS="telephonenumbers";

    //COLOUMN NAMES TABLE EVENTS_DETAILS
    public static final String KEY_CURRE_KEY_ID="id";
    public static final String KEY_CURRE_DATE_TIME="dateandtime";
    public static final String KEY_CURRE_DETAILS="details";
    public static final String KEY_CURRE_EVENT="event";



    //CREATE TABLE CURRENT_EVENTS_DETAILS
    private static final String CREATE_TABLE_EVENTS_DETAILS = "CREATE TABLE "
            + EVENTS_TABLE + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE," + KEY_TIME
            + " TEXT," + KEY_DATE + " TEXT," + KEY_EVENT
            + " TEXT," + KEY_CONFIG_ID + " TEXT,"
            + KEY_TELEPHONE_NUMBERS + " TEXT" + ")";

    //CREATE TABLE CURRENT_EVENTS_DETAILS
    private static final String CREATE_TABLE_CURRENT_EVENTS_DETAILS = "CREATE TABLE IF NOT EXISTS "
            + CURRENT_EVENTS_TABLE + "(" + KEY_CURRE_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
            + KEY_CURRE_DATE_TIME + " TEXT ,"
            + KEY_CURRE_EVENT + " TEXT ,"
            + KEY_CURRE_DETAILS + " TEXT " + ")";


    public EventsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_EVENTS_DETAILS);
        sqLiteDatabase.execSQL(CREATE_TABLE_CURRENT_EVENTS_DETAILS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+CREATE_TABLE_EVENTS_DETAILS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+CREATE_TABLE_CURRENT_EVENTS_DETAILS);
        onCreate(sqLiteDatabase);
    }
}
