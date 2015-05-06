package com.ecg.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{
	private static final String name = "ecg_analysis.db"; 
	private static final int version = 1; 
	@SuppressWarnings("unused")
	private final Context context;
	public DatabaseHelper(Context context) {
	super(context, name, null, version);
		this.context=context;
	}

	public void onCreate(SQLiteDatabase db) {
	}
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
	 public void onOpen(SQLiteDatabase db) {     
         super.onOpen(db);       
     }     
}
