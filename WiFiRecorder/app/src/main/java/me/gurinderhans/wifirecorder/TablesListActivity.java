package me.gurinderhans.wifirecorder;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class TablesListActivity extends Activity {

    public static final String KEY_TABLE_NAME = "tableName";
    private final String TAG = getClass().getSimpleName();
    WiFiDatabaseManager mWifiDatabaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tables_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getActionBar().setElevation(0);

        mWifiDatabaseManager = new WiFiDatabaseManager(this);

        ArrayList<String> tables = mWifiDatabaseManager.getTables();
        tables = new ArrayList<>(tables.subList(1, tables.size()));

        ListView tablesListView = (ListView) findViewById(R.id.tablesListView);

        ArrayAdapter<String> tablesAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tables);

        tablesListView.setAdapter(tablesAdapter);

        tablesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String tableName = ((TextView) view.findViewById(android.R.id.text1)).getText().toString();
                Intent getTableData = new Intent(TablesListActivity.this, TableData.class);
                getTableData.putExtra(KEY_TABLE_NAME, tableName);
                startActivity(getTableData);
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tables_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
