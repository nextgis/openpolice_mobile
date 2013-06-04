/*******************************************************************************
*
* PanicButton
* ---------------------------------------------------------
* Search nearest policeman
*
* Copyright (C) 2013 NextGIS (http://nextgis.ru)
*
* This source is free software; you can redistribute it and/or modify it under
* the terms of the GNU General Public License as published by the Free
* Software Foundation; either version 2 of the License, or (at your option)
* any later version.
*
* This code is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
* details.
*
* A copy of the GNU General Public License is available on the World Wide Web
* at <http://www.gnu.org/copyleft/gpl.html>. You can also obtain it by writing
* to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
* MA 02111-1307, USA.
*
*******************************************************************************/
package com.nextgis.panicbutton;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PolicemanDatabaseHelper extends SQLiteOpenHelper {
	
	public static final String TABLE = "oppo";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_LAT = "lat";
	public static final String COLUMN_LON = "lon";
	public static final String COLUMN_PID = "pid";
	public static final String COLUMN_PRANK = "prank";
	public static final String COLUMN_PPHONE = "pphone";
	public static final String COLUMN_PURL = "purl";
	public static final String COLUMN_PADDR = "paddr";
	public static final String COLUMN_PNAME = "pname";
	
	public static final String DATABASE_NAME = "policeman.db";
	public static final int DATABASE_VERSION = 1;
	
	private static final String DATABASE_CREATE = "create table "
			+ TABLE + "( " 
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_LAT + " real," 
			+ COLUMN_LON + " real," 
			+ COLUMN_PID + " text, "
			+ COLUMN_PNAME + " text, " 
			+ COLUMN_PRANK + " text, " 
			+ COLUMN_PPHONE + " text, " 
			+ COLUMN_PURL + " text, "
			+ COLUMN_PADDR + " text );";

	
	public PolicemanDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create database
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
	}
	
	public void onDrop(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
	}
}
