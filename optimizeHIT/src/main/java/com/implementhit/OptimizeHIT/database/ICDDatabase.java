package com.implementhit.OptimizeHIT.database;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.api.AddSuperbillHandler;
import com.implementhit.OptimizeHIT.models.ICDRecordExtended;
import com.implementhit.OptimizeHIT.models.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class ICDDatabase {
	private static final String DATABASE_NAME = "icd10_codes_3.db";
	
	private static final int SELECT_BLOCK_LIMIT = 500;

	private static final String CODES = "codes";
    private static final String CODE = "code";
    private static final String TITLE = "title";
    private static final String BILLABLE = "billable";
    
    private static final String SUPERBILLS = "superbills";
    private static final String GROUP = "group_name";
    private static final String ORDERING = "ordering";

    private static ICDDatabase sharedDatabase;
    
    private Context context;
    private SQLiteDatabase db;
    
    private ArrayList<ICDRecordExtended> superbill; 
    
    public static ICDDatabase sharedDatabase(Context context) {
    	if (sharedDatabase == null) {
    		sharedDatabase = new ICDDatabase(context);
    	}
    	
    	return sharedDatabase;
    }
	
	private ICDDatabase(Context context) {
        this.context = context;
        boolean isNewDatabase = openDatabase();
        
        if (isNewDatabase) {
			db.execSQL("DROP TABLE IF EXISTS catalog;");
			db.execSQL("DROP TABLE IF EXISTS code;");
        	db.execSQL("CREATE TABLE IF NOT EXISTS " + CODES + " ( "
        		+ CODE + " TEXT PRIMARY KEY NOT NULL, "
				+ BILLABLE + " INTEGER NOT NULL, "
				+ TITLE + " TEXT NOT NULL ); ");
        	db.execSQL("CREATE TABLE IF NOT EXISTS " + SUPERBILLS + " ( "
        		+ CODE + " TEXT PRIMARY KEY NOT NULL, "
				+ GROUP + " INTEGER NOT NULL, "
				+ ORDERING + " TEXT NOT NULL ); ");
        }
        
        superbill = populateUserSuperbill(null);
    }
	
	public ICDRecordExtended[] getChildIcdCodes(String parentCode) {
		String globQuery = "?";
		
		for (int loop = 0 ; loop < 5 ; loop++) {
			Cursor cursor = db.rawQuery("SELECT * FROM " + CODES + " WHERE " + CODE + " GLOB "
                    + DatabaseUtils.sqlEscapeString(parentCode + globQuery), null);

			cursor.moveToFirst();
			
			if (cursor.getCount() == 0) {
				globQuery = globQuery + "?";
				continue;
			}
			
			ICDRecordExtended[] records = new ICDRecordExtended[cursor.getCount()];
			
			int billableIndex = cursor.getColumnIndex(BILLABLE);
			int codeIndex = cursor.getColumnIndex(CODE);
			int titleIndex = cursor.getColumnIndex(TITLE);
			int index = 0;
			
			while (!cursor.isAfterLast()) {
				records[index] = new ICDRecordExtended(
						cursor.getString(codeIndex),
						cursor.getString(titleIndex),
						cursor.getInt(billableIndex) == 1,
						0,
						null);
				cursor.moveToNext();
				index++;
			}
			
			return records;
		}
		
		return new ICDRecordExtended[0];
	}
	
	public void storeSuperbills(JSONArray recordsArray) {
		db.delete(SUPERBILLS, null, null);

		if (recordsArray.length() > 0) {
			String[] reactivationsQueries = insertRecordsQuery(recordsArray);
			for (int block = reactivationsQueries.length - 1; block >= 0; block--) {
				db.execSQL(reactivationsQueries[block]);
			}
		}
		
        superbill = populateUserSuperbill(null);
	}

	private String[] insertRecordsQuery(JSONArray recordsArray) {
		int blockNumber = recordsArray.length() / SELECT_BLOCK_LIMIT + 1;
		int entitiesOffset = recordsArray.length();
		String[] queries = new String[blockNumber];
		
		for (int blockIndex = 0; blockIndex < blockNumber; blockIndex++) {
			StringBuilder supebillsQuery = new StringBuilder("REPLACE INTO " + SUPERBILLS + " ( "
					+ CODE + " , "
					+ ORDERING + " , "
					+ GROUP + " )  VALUES ");
	
			JSONObject record;
			int limit = entitiesOffset - SELECT_BLOCK_LIMIT > 0 ? entitiesOffset - SELECT_BLOCK_LIMIT : 0;
	
			for (int index = entitiesOffset - 1; index >= limit; index--) {
				record = recordsArray.optJSONObject(index);

				supebillsQuery = supebillsQuery
						.append("( ")
						.append(DatabaseUtils.sqlEscapeString(record.optString(CODE))).append(",")
						.append(record.optInt(ORDERING)).append(",")
						.append(DatabaseUtils.sqlEscapeString(record.optString("group"))).append(")");
	
				if (index != limit) {
					supebillsQuery = supebillsQuery.append(",");
				}
			}
			
			entitiesOffset = entitiesOffset - SELECT_BLOCK_LIMIT;
	
			supebillsQuery = supebillsQuery.append(";");
			queries[blockIndex] = supebillsQuery.toString();
		}

		return queries;
	}
	
	public ArrayList<ICDRecordExtended> getUserSuperbill(String group) {
		if (superbill.size() == 0) {
			superbill = populateUserSuperbill(null);
		}
		
		if (group == null) {
			return superbill;
		}
		
		ArrayList<ICDRecordExtended> records = new ArrayList<>();
		
		for (ICDRecordExtended record : superbill) {
			if (record.getGroup().equals(group)) {
				records.add(record);
			}
		}
		
		return records;
	}
	
	public ArrayList<ICDRecordExtended> getUserGroups() {
		ArrayList<ICDRecordExtended> groups = new ArrayList<>();

		for (ICDRecordExtended record : superbill) {
			boolean added = false;
			
			for (ICDRecordExtended group : groups) {
				if (group.getCode().equals(record.getGroup())) {
					added = true;
				}
			}
			
			if (!added) {
				groups.add(new ICDRecordExtended(record.getGroup(), null, false, 0, null));
			}
		}
		
		return groups;
	}
	
	private ArrayList<ICDRecordExtended> populateUserSuperbill(String group) {
		ArrayList<ICDRecordExtended> records = new ArrayList<>();

		String query = "SELECT "
				+ CODES + "." + CODE + " AS " + CODES + "_" + CODE + ","
				+ CODES + "." + TITLE + " AS " + CODES + "_" + TITLE + ","
				+ CODES + "." + BILLABLE + " AS " + CODES + "_" + BILLABLE + ","
				+ SUPERBILLS + "." + GROUP + " AS " + SUPERBILLS + "_" + GROUP + ","
				+ SUPERBILLS + "." + ORDERING + " AS " + SUPERBILLS + "_" + ORDERING
				+ " FROM " + CODES
				+ " JOIN " + SUPERBILLS + " ON " + SUPERBILLS + "." + CODE + " = " + CODES + "." + CODE;

		if (group != null) {
			query = query + " WHERE " + SUPERBILLS + "_" + ORDERING + " = " + group;
		}
		
		query = query + " ORDER BY " + SUPERBILLS + "_" + ORDERING + " ASC, "
			+ CODES + "_" + CODE + " COLLATE NOCASE ASC; ";
		
		Cursor cursor = db.rawQuery(query, null);
		cursor.moveToFirst();
		
		int codeIndex = cursor.getColumnIndex(CODES + "_" + CODE);
		int descriptionIndex = cursor.getColumnIndex(CODES + "_" + TITLE);
		int billableIndex = cursor.getColumnIndex(CODES + "_" + BILLABLE);
		int groupIndex = cursor.getColumnIndex(SUPERBILLS + "_" + GROUP);
		int orderingIndex = cursor.getColumnIndex(SUPERBILLS + "_" + ORDERING);

		while (!cursor.isAfterLast()) {
			ICDRecordExtended record = new ICDRecordExtended(
					cursor.getString(codeIndex),
					cursor.getString(descriptionIndex),
					cursor.getInt(billableIndex) == 1,
					orderingIndex,
					cursor.getString(groupIndex));

			records.add(record);
			
			cursor.moveToNext();
		}

		return records;
	}
	
	public boolean hasSuperbill(String code) {
		for (int index = 0 ; index < superbill.size() ; index++) {
			if (superbill.get(index).getCode().equals(code)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean openDatabase() {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        boolean isNewDatabase = false;

        if (!dbFile.exists()) {
            try {
            	isNewDatabase = true;
                copyDatabase(dbFile);
            } catch (IOException e) {
                throw new RuntimeException("Error creating source database", e);
            }
        }

        db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);

        //TODO: REMOVE FOR PRODUCTION
		Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

		if (c.moveToFirst()) {
			while ( !c.isAfterLast() ) {
				Log.d("TABLES", "Table Name=> "+c.getString(0));
				c.moveToNext();
			}
		}

        return isNewDatabase;
    }

    private void copyDatabase(File dbFile) throws IOException {
    	dbFile.getParentFile().mkdirs();
    	
        InputStream is = context.getAssets().open(DATABASE_NAME);
        OutputStream os = new FileOutputStream(dbFile);

        byte[] buffer = new byte[1024];
        while (is.read(buffer) > 0) {
            os.write(buffer);
        }

        os.flush();
        os.close();
        is.close();
    }

	public void addSuperbill(String code, String group, 
			String description, boolean billable,
			int ordering, AddSuperbillHandler handler) {

        StringBuilder supebillsQuery = new StringBuilder("REPLACE INTO " + SUPERBILLS + " ( "
				+ CODE + " , "
				+ ORDERING + " , "
				+ GROUP + " )  VALUES ");

		supebillsQuery
			.append("( ")
			.append(DatabaseUtils.sqlEscapeString(code)).append(",")
			.append(ordering).append(",")
			.append(DatabaseUtils.sqlEscapeString(group) ).append(");");

		db.execSQL(supebillsQuery.toString());
		
		superbill = populateUserSuperbill(null);
		
		APITalker.sharedTalker().addSuperbill(
				User.sharedUser(context).hash(),
                code,
				description,
				billable,
				group,
				handler);
	}
	
	public void removeSuperbill(String code) {

		db.delete(SUPERBILLS, CODE + " = " + DatabaseUtils.sqlEscapeString(code), null);

        for (Iterator<ICDRecordExtended> recordsIterator = superbill.iterator(); recordsIterator.hasNext(); ) {
            ICDRecordExtended record = recordsIterator.next();

            if ( record.getCode().equals(code) ) {
                recordsIterator.remove();
            }
        }

		superbill = populateUserSuperbill(null);
		
		APITalker.sharedTalker().removeSuperbill(
				User.sharedUser(context).hash(),
				code);

	}
	
	public void flushCodes() {
		db.delete(SUPERBILLS, null, null);
		superbill = new ArrayList<>();
	}
	
	public Context getContext() {
		return context;
	}
}