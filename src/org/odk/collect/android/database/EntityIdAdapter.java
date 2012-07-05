package org.odk.collect.android.database;

import java.io.File;
import java.util.Set;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class EntityIdAdapter {
	
	private final static String t = "EntityIdAdapter";

	public static final String KEY_ID = "_id";
	public static final String KEY_EXTID = "extId";
	
	// database columns individual
	public static final String KEY_INDIVIDUAL_FIRST = "firstname";
	public static final String KEY_INDIVIDUAL_LAST = "lastname";
	public static final String KEY_INDIVIDUAL_GENDER = "gender";
	
	
	// database columns fieldworker
	public static final String KEY_FW_FIRST = "firstname";
	public static final String KEY_FW_LAST = "lastname";

	// database columns location
	public static final String KEY_LOCATION_NAME = "name";
	
	// database columns household
	public static final String KEY_HOUSEOLD_NAME = "name";
	 
	// database columns visit
	public static final String KEY_VISIT_ROUND = "round";
	
	// database columns hierarchy
	public static final String KEY_HIERARCHY_NAME = "name";
	
	// database columns location hierarchy
	public static final String KEY_LOCHIERARCHY_NAME = "name";
//	public static final String KEY_LEVEL= "level_uuid";
	
	public static final String KEY_SOCIALGROUP_EXT_ID = "sgExtId";
	 
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	 
	private static final String INDIVIDUAL_CREATE =
	        "create table individual (_id integer primary key autoincrement, " + 
	        "extId text not null, firstname text not null, lastname text not null, " +
	        "gender text not null);";

	private static final String MEMBERSHIP_CREATE =
	        "create table membership (_id integer, sgExtId text not null, PRIMARY KEY (_id, sgExtId), " +
	        "FOREIGN KEY (_id) REFERENCES individual (_id));";
	
	private static final String RESIDENCY_CREATE =
			"create table residency (_id integer, extId text not null, PRIMARY KEY(_id, extId), " +
			"FOREIGN KEY (_id) REFERENCES individual (_id));";
	
	private static final String FW_CREATE =
        "create table fieldworker (_id integer primary key autoincrement, " + 
        "extId text not null, firstname text not null, lastname text not null);";
	
	private static final String LOCATION_CREATE =
	        "create table location (_id integer primary key autoincrement, " +
	        "extId text not null, name text not null);";
	
	private static final String HOUSEHOLD_CREATE =
	        "create table household (_id integer primary key autoincrement, " +
	        "extId text not null, name text not null);";
	
	private static final String VISIT_CREATE =
	        "create table visit (_id integer primary key autoincrement, " +
	        "extId text not null, round text not null);";
	
	private static final String HIERARCHY_CREATE =
	        "create table hierarchy (_id integer primary key autoincrement, " +
	        "extId text not null, name text not null);";
	
	/*private static final String LOCATIONHIERARCHY_CREATE =
        "create table locationhierarchy (_id integer primary key autoincrement, " +
        "extId text not null, name text not null, level_uuid text not null);";*/
	
	private static final String LOCATIONHIERARCHY_CREATE =
        "create table locationhierarchy (_id integer primary key autoincrement, " +
        "extId text not null, name text not null);";
	
	 
	 private static final String DATABASE_NAME = "entityData";
	 private static final String DATABASE_TABLE_INDIVIDUAL = "individual";
	 private static final String DATABASE_TABLE_LOCATION = "location";
	 private static final String DATABASE_TABLE_HOUSEHOLD = "household";
	 private static final String DATABASE_TABLE_VISIT = "visit";
	 private static final String DATABASE_TABLE_FW = "fieldworker";
	 private static final String DATABASE_TABLE_HIERARCHY = "hierarchy";
	 private static final String DATABASE_TABLE_LOCHIERARCHY = "locationhierarchy";
	 private static final String DATABASE_TABLE_MEMBERSHIP = "membership";
	 private static final String DATABASE_TABLE_RESIDENCY = "residency";
	 private static final int DATABASE_VERSION = 1;
	 private static final String DATABASE_PATH = Environment.getExternalStorageDirectory() + "/odk/metadata";
	 
	 private static class DatabaseHelper extends ODKSQLiteOpenHelper {

        DatabaseHelper() {
            super(DATABASE_PATH, DATABASE_NAME, null, DATABASE_VERSION);

            // Create database storage directory if it doesn't not already exist.
            File f = new File(DATABASE_PATH);
            f.mkdirs();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(INDIVIDUAL_CREATE);
            db.execSQL(LOCATION_CREATE);
            db.execSQL(HOUSEHOLD_CREATE);
            db.execSQL(VISIT_CREATE);
            db.execSQL(HIERARCHY_CREATE);
            db.execSQL(FW_CREATE);
            db.execSQL(LOCATIONHIERARCHY_CREATE);
            db.execSQL(MEMBERSHIP_CREATE);
            db.execSQL(RESIDENCY_CREATE);
        }

        @Override
        // upgrading will destroy all old data
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_MEMBERSHIP);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_RESIDENCY);
        	db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_INDIVIDUAL);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_LOCATION);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_HOUSEHOLD);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_VISIT);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_FW);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_HIERARCHY);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_LOCHIERARCHY);
            onCreate(db);
        }
    }
	 
	public EntityIdAdapter() { }
	
    public EntityIdAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper();
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
        mDb.close();
    }
    
    public long createIndividual(String extId, String firstname, String lastname, String gender, String location, Set<String> memberships) {
        ContentValues cv = new ContentValues();

        cv.put(KEY_EXTID, extId);
        cv.put(KEY_INDIVIDUAL_FIRST, firstname);
        cv.put(KEY_INDIVIDUAL_LAST, lastname);
        cv.put(KEY_INDIVIDUAL_GENDER, gender);

        long id = -1;
        mDb.beginTransaction();
        try {	
        	id = mDb.insertOrThrow(DATABASE_TABLE_INDIVIDUAL, null, cv);
        	
			if (!TextUtils.isEmpty(location)) {
				cv.clear();
				cv.put(KEY_ID, id);
				cv.put(KEY_EXTID, location);
				mDb.insertOrThrow(DATABASE_TABLE_RESIDENCY, null, cv);
			}

        	for(String membership : memberships) {
        		cv.clear();
        		cv.put(KEY_ID, id);
        		cv.put(KEY_SOCIALGROUP_EXT_ID, membership);
        		mDb.insertOrThrow(DATABASE_TABLE_MEMBERSHIP, null, cv);
        	}
        	
        	mDb.setTransactionSuccessful();
        } catch (SQLiteConstraintException e) {
        	id = -1;
            Log.e(t, "Caught SQLiteConstraitException: " + e);
        } finally {
        	mDb.endTransaction();
        }
        
        return id;
    }
    
    
    public long createFieldworker(String extId, String firstname, String lastname) {
        ContentValues cv = new ContentValues();

        cv.put(KEY_EXTID, extId);
        cv.put(KEY_FW_FIRST, firstname);
        cv.put(KEY_FW_LAST, lastname);


        long id = -1;
        try {	
        	id = mDb.insertOrThrow(DATABASE_TABLE_FW, null, cv);
        } catch (SQLiteConstraintException e) {
            Log.e(t, "Caught SQLiteConstraitException: " + e);
        }
        return id;
    }
    
    
    public long createLocation(String extId, String name) {
        ContentValues cv = new ContentValues();

        cv.put(KEY_EXTID, extId);
        cv.put(KEY_LOCATION_NAME, name);

        long id = -1;
        try {	
        	id = mDb.insertOrThrow(DATABASE_TABLE_LOCATION, null, cv);
        } catch (SQLiteConstraintException e) {
            Log.e(t, "Caught SQLiteConstraitException: " + e);
        }
        return id;
    }
    
    public long createHousehold(String extId, String name) {
        ContentValues cv = new ContentValues();

        cv.put(KEY_EXTID, extId);
        cv.put(KEY_HOUSEOLD_NAME, name);

        long id = -1;
        try {	
        	id = mDb.insertOrThrow(DATABASE_TABLE_HOUSEHOLD, null, cv);
        } catch (SQLiteConstraintException e) {
            Log.e(t, "Caught SQLiteConstraitException: " + e);
        }
        return id;
    }
    
    public long createVisit(String extId, String round) {
        ContentValues cv = new ContentValues();

        cv.put(KEY_EXTID, extId);
        cv.put(KEY_VISIT_ROUND, round);

        long id = -1;
        try {	
        	id = mDb.insertOrThrow(DATABASE_TABLE_VISIT, null, cv);
        } catch (SQLiteConstraintException e) {
            Log.e(t, "Caught SQLiteConstraitException: " + e);
        }
        return id;
    }
    
    public long createHierarchy(String extId, String name) {
        ContentValues cv = new ContentValues();

        cv.put(KEY_EXTID, extId);
        cv.put(KEY_HIERARCHY_NAME, name);
       
        long id = -1;
        try {	
        	id = mDb.insertOrThrow(DATABASE_TABLE_HIERARCHY, null, cv);
        } catch (SQLiteConstraintException e) {
            Log.e(t, "Caught SQLiteConstraitException: " + e);
        }
        return id;
    }
    
    
    
   /* public long createLocHierarchy(String extId, String name, String level) {
        ContentValues cv = new ContentValues();

        cv.put(KEY_EXTID, extId);
        cv.put(KEY_LOCHIERARCHY_NAME, name);
        cv.put(KEY_LEVEL, level);
       
        long id = -1;
        try {	
        	id = mDb.insertOrThrow(DATABASE_TABLE_LOCHIERARCHY, null, cv);
        } catch (SQLiteConstraintException e) {
            Log.e(t, "Caught SQLiteConstraitException: " + e);
        }
        return id;
    }
    */
    
    public long createLocHierarchy(String extId, String name) {
        ContentValues cv = new ContentValues();

        cv.put(KEY_EXTID, extId);
        cv.put(KEY_LOCHIERARCHY_NAME, name);
       
        long id = -1;
        try {	
        	id = mDb.insertOrThrow(DATABASE_TABLE_LOCHIERARCHY, null, cv);
        } catch (SQLiteConstraintException e) {
            Log.e(t, "Caught SQLiteConstraitException: " + e);
        }
        return id;
    }
    
    public boolean deleteIndividual(long id) {
        return mDb.delete(DATABASE_TABLE_INDIVIDUAL, KEY_ID + "='" + id + "'", null) > 0;
    }
    
    public boolean deleteFieldWorker(long id) {
        return mDb.delete(DATABASE_TABLE_FW, KEY_ID + "='" + id + "'", null) > 0;
    }
    
    public boolean deleteLocation(long id) {
        return mDb.delete(DATABASE_TABLE_LOCATION, KEY_ID + "='" + id + "'", null) > 0;
    }
    
    public boolean deleteHousehold(long id) {
        return mDb.delete(DATABASE_TABLE_HOUSEHOLD, KEY_ID + "='" + id + "'", null) > 0;
    }
    
    public boolean deleteVisit(long id) {
        return mDb.delete(DATABASE_TABLE_VISIT, KEY_ID + "='" + id + "'", null) > 0;
    }
    
    public boolean deleteHierarchy(long id) {
        return mDb.delete(DATABASE_TABLE_HIERARCHY, KEY_ID + "='" + id + "'", null) > 0;
    }
    public boolean deleteLocHierarchy(long id) {
        return mDb.delete(DATABASE_TABLE_LOCHIERARCHY, KEY_ID + "='" + id + "'", null) > 0;
    }
    public SQLiteDatabase getmDb() {
		return mDb;
	}
}
