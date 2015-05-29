package me.gurinderhans.sfumaps.wifirecorder.Controller;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.gurinderhans.sfumaps.R;

/**
 * Created by ghans on 15-05-28.
 */
public class ListDataTablesActivity extends ListActivity {

    private List<Map<String, Object>> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set activity theme to light text
        this.setTheme(R.style.RecorderActivity);

        // initial setup
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_recorded_data_tables);

        data = new ArrayList<>();

        Map<String, Object> item;
        item = new HashMap<>();
        item.put("1", "A");
        item.put("2", "B");
        data.add(item);
        item = new HashMap<>();
        item.put("3", "C");
        item.put("4", "D");
        data.add(item);
        item = new HashMap<>();
        item.put("5", "E");
        item.put("6", "F");
        data.add(item);


        SimpleAdapter myAdapter = new SimpleAdapter(this, data, android.R.layout.simple_list_item_1, new String[]{"AAA"}, new int[]{android.R.id.text1});
        setListAdapter(myAdapter);


    }
}
