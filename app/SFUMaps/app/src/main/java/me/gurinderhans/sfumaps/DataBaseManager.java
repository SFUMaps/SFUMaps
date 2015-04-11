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

    public static final String DATABASE_NAME = "wifi_data";
    public static final int DATABASE_VERSION = 1;
    public static final String ASSETS_DB_PATH = "databases/";
    private static String databasePath = "";
    private final String TAG = getClass().getSimpleName();
    Context context;

    public DataBaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.context = context;

        databasePath = context.getDatabasePath("wifi_data").getPath();

        this.createDataBase();

    }

    /**
     * @return - list of all database tables
     */
    ArrayList<String> getTableNames() {

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> tables = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (cursor.moveToFirst()) {
            do tables.add(cursor.getString(0));
            while (cursor.moveToNext() && !(cursor.getString(0).equals("android_metadata")));
        } else Log.i("ERROR", "Unable to move cursor!");

        cursor.close();
        db.close();

        return tables;
    }

    /**
     * @param tablename - the name of the table that we want the data from
     * @return - the table data
     */
    ArrayList<HashMap<String, Object>> getTableData(String tablename) {
        SQLiteDatabase db = getReadableDatabase();

        ArrayList<HashMap<String, Object>> data = new ArrayList<>();

        String GET_TABLE_DATA_QUERY = "SELECT * FROM " + tablename;

        Cursor cursor = db.rawQuery(GET_TABLE_DATA_QUERY, null);

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