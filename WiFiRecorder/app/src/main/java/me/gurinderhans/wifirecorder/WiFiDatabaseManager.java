package me.gurinderhans.wifirecorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by ghans on 14-12-10.
 */
public class WiFiDatabaseManager extends SQLiteOpenHelper {
    private final String TAG = getClass().getSimpleName();

    public static final String DATABASE_NAME = "wifi_data";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "apsdata";

    // KEYS for Table
    public static final String KEY_ROWID = "_id";
    public static final String KEY_SSID = "ssid";
    public static final String KEY_BSSID = "bssid";
    public static final String KEY_FREQ = "freq";
    public static final String KEY_LEVEL = "level";
    public static final String KEY_TIME = "rec_time";

    public static final String DBPATH = "/WiFiRecorder/dbs/";

    Context context;

    public WiFiDatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.i("onUpgrade", "Old version: " + oldVersion + " New version: " + newVersion);
    }

    // Adding new contact
    void addApData(String ssid, String bssid, String freq, String level, String time, String tbl_name) {
        SQLiteDatabase db = getWritableDatabase();

        Log.i(TAG, "recording data");

        String table = TABLE_NAME + "_" + tbl_name;

        String CREATE_CONTACTS_TABLE = "CREATE TABLE IF NOT EXISTS " + table + " (" + KEY_ROWID + " INTEGER PRIMARY KEY, " +
                KEY_SSID + " TEXT, " +
                KEY_BSSID + " TEXT, " +
                KEY_FREQ + " TEXT, " +
                KEY_LEVEL + " TEXT, " +
                KEY_TIME + " TEXT)";
        db.execSQL(CREATE_CONTACTS_TABLE);

        ContentValues values = new ContentValues();
        values.put(KEY_SSID, ssid);
        values.put(KEY_BSSID, bssid);
        values.put(KEY_FREQ, freq);
        values.put(KEY_LEVEL, level);
        values.put(KEY_TIME, time);

        // Inserting Row
        db.insert(table, null, values);
        db.close(); // Closing database connection

    }

    public File exportDB() {
        File sd = Environment.getExternalStorageDirectory(),
                data = Environment.getDataDirectory();

        //make sure folder we're copying to exists
        File folder = new File(sd + DBPATH);
        boolean success = true;

        if (!folder.exists()) success = folder.mkdirs();

        if(success) {

            FileChannel source, destination;

            String currentDBPath = "/data/" + getClass().getPackage().getName() + "/databases/" + DATABASE_NAME,
                    backupDBPath = DBPATH + DATABASE_NAME;

            File currentDB = new File(data, currentDBPath),
                    backupDB = new File(sd, backupDBPath);

            try {
                source = new FileInputStream(currentDB).getChannel();
                destination = new FileOutputStream(backupDB).getChannel();
                destination.transferFrom(source, 0, source.size());
                source.close();
                destination.close();
                Toast.makeText(context, "DB Exported", Toast.LENGTH_SHORT).show();
                return backupDB;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}