package me.gurinderhans.sfumaps.factory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import me.gurinderhans.sfumaps.app.Keys;
import me.gurinderhans.sfumaps.factory.classes.WiFiAccessPoint;
import me.gurinderhans.sfumaps.app.AppConfig;

/**
 * Created by ghans on 1/24/15.
 */
public class DataBaseManager extends SQLiteOpenHelper {

	public static final String TAG = DataBaseManager.class.getSimpleName();

	// constants
	public static final String DATABASE_NAME = "WIFI_DATA";
	public static final int DATABASE_VERSION = 1;
	public static final String ASSETS_DATABASE_PATH = "databases/" + DATABASE_NAME;
	public static final String TABLE_NAME = "apsdata";
	// singleton variable
	private static DataBaseManager mInstance = null;
	Context context;
	// member variables
	private boolean createDb = false, upgradeDb = false;

	private DataBaseManager(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = ctx;
	}

	public static DataBaseManager getInstance(Context ctx) {

		if (mInstance == null) {
			mInstance = new DataBaseManager(ctx);
		}
		return mInstance;
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		createDb = true;
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
		upgradeDb = true;
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);

		if (createDb) {
			createDb = false;
			copyDatabaseFromAssets(db);
		}

		if (upgradeDb) {
			upgradeDb = false;
		}
	}


	/**
	 * add an access point to the specified database table
	 *
	 * @param point   - Access Point
	 * @param tblName - table name to add the access point to
	 */
	public void addAccessPoint(WiFiAccessPoint point, String tblName) {

		// get a writable database
		SQLiteDatabase db = getWritableDatabase();

		String table = TABLE_NAME + "_" + tblName;

		String CREATE_CONTACTS_TABLE = "CREATE TABLE IF NOT EXISTS " + table + " (" + Keys.KEY_ROWID + " INTEGER PRIMARY KEY, " +
				Keys.KEY_SSID + " TEXT, " +
				Keys.KEY_BSSID + " TEXT, " +
				Keys.KEY_FREQ + " TEXT, " +
				Keys.KEY_RSSI + " TEXT, " +
				Keys.KEY_TIME + " TEXT)";
		db.execSQL(CREATE_CONTACTS_TABLE);

		ContentValues values = new ContentValues();
		values.put(Keys.KEY_SSID, point.SSID);
		values.put(Keys.KEY_BSSID, point.BSSID);
		values.put(Keys.KEY_RSSI, point.RSSI);
		values.put(Keys.KEY_FREQ, point.FREQ);
		values.put(Keys.KEY_TIME, point.TIME);

		// insert row
		db.insert(table, null, values);
		db.close(); // Closing database connection
	}


	/**
	 * Copies the database file stored in assets folder to the
	 * application database location
	 *
	 * @param db - application database that we copy the contents to ( --> )
	 */
	private void copyDatabaseFromAssets(SQLiteDatabase db) {

		InputStream inputStream = null;
		OutputStream outputStream = null;

		try {
			// Open db packaged as asset as the input stream
			inputStream = context.getAssets().open(ASSETS_DATABASE_PATH);

			// Open the db in the application package context:
			outputStream = new FileOutputStream(db.getPath());

			// Transfer db file contents:
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}
			outputStream.flush();

			// Set the version of the copied database to the current version
			SQLiteDatabase copiedDb = context.openOrCreateDatabase(
					DATABASE_NAME, 0, null);
			copiedDb.execSQL("PRAGMA user_version = " + DATABASE_VERSION);
			copiedDb.close();

		} catch (IOException e) {
			e.printStackTrace();
			Log.i(TAG, "Error copying database");
		} finally {
			try { // Close the streams
				if (outputStream != null)
					outputStream.close();

				if (inputStream != null)
					inputStream.close();

			} catch (IOException e) {
				e.printStackTrace();
				Log.i(TAG, "Error closing streams");
			}
		}
	}

	/**
	 * @return - list of all database tables
	 */
	public ArrayList<String> getDataTables() {

		SQLiteDatabase db = getReadableDatabase();
		ArrayList<String> tables = new ArrayList<>();

		Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

		if (cursor.moveToFirst()) {
			do {
				String tableName = cursor.getString(0);
				if (tableName.startsWith(AppConfig.DATABASE_TABLE_PREFIX))
					tables.add(tableName);
			} while (cursor.moveToNext());

		} else {
			Log.i("ERROR", "Unable to move cursor!");
		}

		cursor.close();
		db.close();

		return tables;
	}

	/**
	 * @param tablename - the name of the table that we want the data from
	 * @return - the table data
	 */
	public ArrayList<HashMap<String, Object>> getTableData(String tablename) {
		SQLiteDatabase db = getReadableDatabase();

		ArrayList<HashMap<String, Object>> data = new ArrayList<>();

		Cursor cursor = db.query(tablename, null, null, null, null, null, null);

		if (cursor.moveToFirst()) {
			do {

				HashMap<String, Object> tableRow = new HashMap<>();
				tableRow.put(Keys.KEY_ROWID, cursor.getString(0));
				tableRow.put(Keys.KEY_SSID, cursor.getString(1));
				tableRow.put(Keys.KEY_BSSID, cursor.getString(2));
				tableRow.put(Keys.KEY_FREQ, cursor.getString(3));
				tableRow.put(Keys.KEY_RSSI, cursor.getString(4));
				tableRow.put(Keys.KEY_TIME, cursor.getString(5));

				data.add(tableRow);

			} while (cursor.moveToNext());
		}

		cursor.close();
		db.close();

		return data;
	}


    /*
     * get campus hierarchy
     */
    /*void getHierarchy() {

        SQLiteDatabase db = getReadableDatabase();

        Tree<HashMap<Integer, String>> t;

        String GET_HIERARCHY_QUERY = "SELECT * FROM hierarchy";
        Cursor cursor = db.rawQuery(GET_HIERARCHY_QUERY, null);

        if (cursor.moveToFirst()) {

            Log.i(TAG, "id: " + cursor.getInt(0));
            int id = cursor.getInt(0);
            int parentNode = cursor.getInt(2);
            String selfName = cursor.getString(1);
            int selfId = cursor.getInt(3);
            String value = cursor.getString(4);

            final int tmpId = id;
            final String tmpName = selfName;

            t= new Tree<HashMap<Integer, String>>(new HashMap<Integer, String>(){{
                put(tmpId, tmpName);
            }});

            Log.i(TAG, t.toString());

            // we read the first value manually so we need
            // to move cursor so we don't read the first row again
            cursor.moveToPosition(1);

            do {

                id = cursor.getInt(0);
                parentNode = cursor.getInt(2);
                selfName = cursor.getString(1);
                selfId = cursor.getInt(3);
                value = cursor.getString(4);

                // traverse through the tree and find the parent node
//                t.addLeaf(parent, this);

//                Log.i(TAG, "id: " + id);


            } while (cursor.moveToNext());
        }

//        Log.i(TAG, t.toString());

        cursor.close();
        db.close();
    }*/

}