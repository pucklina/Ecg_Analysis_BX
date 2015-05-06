package com.ecg.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_DECTIME = "dectime";
    public static final String KEY_BEATTYPE = "type";
    public static final String KEY_QRSWIDTH = "qrswidth";
    public static final String KEY_RRINTERVAL = "rr";
    public String DATABASE_TABLE = "TB_";
    private final Context context;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    // ---打开数据库---
    public DBAdapter opendropold(String fileName) throws SQLException {
        db = DBHelper.getWritableDatabase();
        DATABASE_TABLE += fileName;
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        db.execSQL("CREATE TABLE " + DATABASE_TABLE + " (" + KEY_ROWID
                + " integer primary key autoincrement, " + KEY_DECTIME
                + " INTEGER, " + KEY_BEATTYPE + " VARCHAR(1)," + KEY_QRSWIDTH
                + " INTEGER," + KEY_RRINTERVAL + " INTEGER)");
        Log.w("DBAdapter", "open &drop old!");
        return this;
    }
    public DBAdapter open(String fileName) throws SQLException {
        if(db.isOpen())
            db.close();
        try{
            Thread.sleep(1000);
        }
        catch(Exception e)
        {}
        db = DBHelper.getReadableDatabase();
        DATABASE_TABLE += fileName;
        Log.w("DBAdapter", "open!");
        return this;
    }

    // ---关闭数据库---

    public void close() {
        db.close();
        DBHelper.close();
        Log.w("DBAdapter", "dbhelper close!");
    }

    public long insertAnno(Anno anno) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_DECTIME, anno.dectTime);
        initialValues.put(KEY_BEATTYPE, anno.beatType);
        initialValues.put(KEY_QRSWIDTH, anno.qrsWidth);
        initialValues.put(KEY_RRINTERVAL, anno.RR);
        return db.insert(DATABASE_TABLE, null, initialValues);

    }

    // ---检索一个指定标题---

    public int getTypeCount(String beatType) throws SQLException {
        int i = 0;
        String selectQ = "SELECT * FROM " + DATABASE_TABLE + " WHERE "
                + KEY_BEATTYPE + "='" + beatType+"'";
        Cursor mCursor = db.rawQuery(selectQ, null);
        if (mCursor != null) {
            i=mCursor.getCount();
        }
        mCursor.close();
        return i;
    }
    public int getRRCount(double RR) throws SQLException {
        int i = 0;
        String selectQ = "SELECT * FROM " + DATABASE_TABLE + " WHERE "
                + KEY_RRINTERVAL + "<'" + RR+"'";
        Cursor mCursor = db.rawQuery(selectQ, null);
        if (mCursor != null) {
            i=mCursor.getCount();
        }
        mCursor.close();
        return i;
    }



}