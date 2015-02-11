package me.gurinderhans.sfumaps;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghans on 1/24/15.
 */
public class DataBaseManager extends SQLiteOpenHelper {

    private final String TAG = getClass().getSimpleName();

    private static String databasePath = "";

    public static final String DATABASE_NAME = "wifi_data";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "apsdata";
    public static final String ASSETS_DB_PATH = "databases/";

    // KEYS for Table - - - - - - - - - -
    public static final String KEY_ROWID = "_id";
    public static final String KEY_SSID = "ssid";
    public static final String KEY_BSSID = "bssid";
    public static final String KEY_FREQ = "freq";
    public static final String KEY_RSSI = "level";
    public static final String KEY_TIME = "rec_time";
//    public static final String KEY_TABLE_NAME = "tableName";

    Context context;

    public DataBaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

        databasePath = context.getDatabasePath("wifi_data").getPath();

        this.createDataBase();

    }

    public ArrayList<String> getTables() {
        SQLiteDatabase db = getReadableDatabase();

        ArrayList<String> tables = new ArrayList<>();

        String GET_TABLES_QUERY = "SELECT name FROM sqlite_master WHERE type='table'";

        Cursor cursor = db.rawQuery(GET_TABLES_QUERY, null);
        if (cursor.moveToFirst()) {
            do tables.add(cursor.getString(0));
            while (cursor.moveToNext() && !(cursor.getString(0).equals("android_metadata")));
        } else {
            //do something if cursor is unable to proceed
            Log.i("ERROR", "Unable to move cursor!");
        }
        db.close();
        return tables;
    }

    public ArrayList<HashMap<String, String>> getTableData(String tablename) {
        SQLiteDatabase db = getReadableDatabase();

        ArrayList<HashMap<String, String>> data = new ArrayList<>();

        String GET_TABLE_DATA_QUERY = "SELECT * FROM " + tablename;

        Cursor cursor = db.rawQuery(GET_TABLE_DATA_QUERY, null);

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> tableRow = new HashMap<>();
//                tableRow.put(KEY_ROWID, cursor.getString(0));
                tableRow.put(KEY_SSID, cursor.getString(1));
                tableRow.put(KEY_BSSID, cursor.getString(2));
                tableRow.put(KEY_FREQ, cursor.getString(3));
                tableRow.put(KEY_RSSI, cursor.getString(4));
                tableRow.put(KEY_TIME, cursor.getString(5));

                data.add(tableRow);
            } while (cursor.moveToNext());
        }
        db.close();

        return data;
    }


    /**
     * Creates a empty database on the system and rewrites it with our own database.
     */
    public void createDataBase() {

        boolean dbExist = checkDataBase();

        if (!dbExist) {
            //By calling this method an empty database will be created into the default system path
            //of the app allowing us to overwrite the database
            this.getReadableDatabase();

            try {
                copyDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * Check if the database already exist to avoid re-copying the file each time the app opens
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) { /* database doesn't exist yet */ }

        if (checkDB != null) checkDB.close();

        return checkDB != null;
    }


    /**
     * Copies your database from the local assets-folder to the just created empty database in the
     * app databases/ folder, from where it can be accessed and handled.
     */
    private void copyDataBase() throws IOException {

        //Open your local db as the input stream
        InputStream myInput = context.getAssets().open(ASSETS_DB_PATH + DATABASE_NAME);

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(databasePath);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;

        while ((length = myInput.read(buffer)) > 0)
            myOutput.write(buffer, 0, length);

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.i("onUpgrade", "Old version: " + oldVersion + " New version: " + newVersion);
    }

}