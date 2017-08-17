package com.juniper.jconference.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.juniper.jconference.db.EventsDBHelper;

import java.util.HashMap;

/**
 * Created by koteswara on 7/7/17.
 */

public class Provider extends ContentProvider{
    //AUTHORITY
    public static final String CONTENT_AUTHORITY = "com.juniper.jconference.provider";
    //URL
    public static final String EVENTS_URL = "content://"+ CONTENT_AUTHORITY +"/"+ EventsDBHelper.EVENTS_TABLE;
    public static final String CURRENT_EVENTS_URL = "content://"+ CONTENT_AUTHORITY +"/"+ EventsDBHelper.CURRENT_EVENTS_TABLE;

    public static final Uri CONTENT_EVENTS_URI = Uri.parse(EVENTS_URL);
    public static final Uri CONTENT_CURRENT_EVENTS_URI = Uri.parse(CURRENT_EVENTS_URL);

    //URI MATCHER ID FOR DATA RETRIVE
    static final int EVENTS = 1;
    static final int EVENTS_ID = 2;
    static final int CURRENT_EVENTS = 3;
    static final int CURRENT_EVENTS_ID = 4;

    /**
     * Database specific constant declarations
     */
    public SQLiteDatabase db;

    //DB HELPER OBJECT DECLARATION
    EventsDBHelper DBHelper;;

    private static HashMap<String, String> values;

    //URI MARTECHER
    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(CONTENT_AUTHORITY, EventsDBHelper.EVENTS_TABLE, EVENTS);
        uriMatcher.addURI(CONTENT_AUTHORITY, EventsDBHelper.EVENTS_TABLE+"/#", EVENTS_ID);
        uriMatcher.addURI(CONTENT_AUTHORITY, EventsDBHelper.CURRENT_EVENTS_TABLE, CURRENT_EVENTS);
        uriMatcher.addURI(CONTENT_AUTHORITY, EventsDBHelper.CURRENT_EVENTS_TABLE+"/#", CURRENT_EVENTS_ID);

    }
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case EVENTS:
                return "vnd.android.cursor.dir/vnd.com.juniper.jconference."+ EventsDBHelper.EVENTS_TABLE;
            case EVENTS_ID:
                return "vnd.android.cursor.dir/vnd.com.juniper.jconference."+ EventsDBHelper.EVENTS_TABLE;
            case CURRENT_EVENTS:
                return "vnd.android.cursor.dir/vnd.com.juniper.jconference."+ EventsDBHelper.CURRENT_EVENTS_TABLE;
            case CURRENT_EVENTS_ID:
                return "vnd.android.cursor.dir/vnd.com.juniper.jconference."+ EventsDBHelper.CURRENT_EVENTS_TABLE;
            default:
                throw new IllegalArgumentException("Invalid URI: "+uri);
        }
    }
    @Override
    public boolean onCreate() {
        DBHelper=new EventsDBHelper(getContext());

        return (db == null)? false:true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case EVENTS:
                qb.setTables(EventsDBHelper.EVENTS_TABLE);
                qb.setProjectionMap(values);
                break;
            case EVENTS_ID:
                qb.setTables(EventsDBHelper.EVENTS_TABLE);
                qb.appendWhere(EventsDBHelper.KEY_EVENT + "=?" + uri.getPathSegments().get(1));
                break;
            case CURRENT_EVENTS:
                qb.setTables(EventsDBHelper.CURRENT_EVENTS_TABLE);
                qb.setProjectionMap(values);
                break;
            case CURRENT_EVENTS_ID:
                qb.setTables(EventsDBHelper.CURRENT_EVENTS_TABLE);
                qb.appendWhere(EventsDBHelper.KEY_CURRE_EVENT + "=?" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException( "illegal uri: " + uri);

        }
        db = DBHelper.getWritableDatabase();
        Cursor c = qb.query(db,	projection,	selection, selectionArgs,null, null, sortOrder);
        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        //   db.close();
        Log.d("inside query", "queried records: "+c.getCount());
        return c;
    }



    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        db=DBHelper.getWritableDatabase();
        long rowID=0;
        /**
         * Add a new  records
         */
        switch (uriMatcher.match(uri)){
            case EVENTS:
                rowID = db.insert(EventsDBHelper.EVENTS_TABLE, null, contentValues);
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            case CURRENT_EVENTS:
                rowID = db.insert(EventsDBHelper.CURRENT_EVENTS_TABLE, null, contentValues);
                getContext().getContentResolver().notifyChange(uri, null);
                break;

        }
        /**
         * If record is added successfully
         */
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(uri, rowID);

            Log.i("uri after insert",_uri.toString());
            return _uri;
        }

        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count =0;
        db=DBHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case EVENTS:
                count = db.delete(EventsDBHelper.EVENTS_TABLE, null, null);
                break;
            case EVENTS_ID:
                String idStr1 = uri.getLastPathSegment();
                String where1 = EVENTS_ID + " = " + idStr1;
                if (!TextUtils.isEmpty(selection)) {
                    where1 += " AND " + selection;
                }
                count = db.delete(EventsDBHelper.EVENTS_TABLE, where1, selectionArgs);
                break;
            case CURRENT_EVENTS:
                count = db.delete(EventsDBHelper.CURRENT_EVENTS_TABLE, null, null);
                break;
            case CURRENT_EVENTS_ID:
                String idStr2 = uri.getLastPathSegment();
                String where2 = CURRENT_EVENTS_ID + " = " + idStr2;
                if (!TextUtils.isEmpty(selection)) {
                    where2 += " AND " + selection;
                }
                count = db.delete(EventsDBHelper.CURRENT_EVENTS_TABLE, where2, selectionArgs);

                break;

            default:
                throw new IllegalArgumentException( "illegal uri: " + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, String wheree, String[] whereArgs) {
        int count =0;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(EventsDBHelper.EVENTS_TABLE);
        db=DBHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case EVENTS:
                count = db.update(EventsDBHelper.KEY_EVENT, contentValues, wheree, whereArgs);

                break;
            case EVENTS_ID:
                String idStr = uri.getLastPathSegment();
                String where = EVENTS_ID + " = " + idStr;
                if (!TextUtils.isEmpty(wheree)) {
                    where += " AND " + wheree;
                }
                count = db.update(EventsDBHelper.EVENTS_TABLE, contentValues, where, whereArgs);
                break;
            case CURRENT_EVENTS:
                count = db.update(EventsDBHelper.CURRENT_EVENTS_TABLE, contentValues, wheree, whereArgs);

                break;
            case CURRENT_EVENTS_ID:
                String idStr1 = uri.getLastPathSegment();
                String where1 = CURRENT_EVENTS_ID + " = " + idStr1;
                if (!TextUtils.isEmpty(wheree)) {
                    where1 += " AND " + wheree;
                }
                count = db.update(EventsDBHelper.CURRENT_EVENTS_TABLE, contentValues, where1, whereArgs);
                break;

            default:
                throw new IllegalArgumentException( "illegal uri: " + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
