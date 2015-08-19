package me.gurinderhans.sfumaps.devtools.wifirecorder.Controller;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.gurinderhans.sfumaps.utils.AppConfig;
import me.gurinderhans.sfumaps.factory.DataBaseManager;
import me.gurinderhans.sfumaps.utils.MapTools;
import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.devtools.wifirecorder.Keys;
import me.gurinderhans.sfumaps.devtools.wifirecorder.View.WifiAPListViewAdapter;
import me.gurinderhans.sfumaps.factory.classes.WiFiAccessPoint;

public class RecordWifiDataActivity extends AppCompatActivity {


	public static final String TAG = RecordWifiDataActivity.class.getSimpleName();

	public static final int TOTAL_SCANS = 20;


	// UI
	ListView mWifiApListView;
	EditText recordDataTableInput;
	Handler mHandler = new Handler();
	String inspectingTable;
	ArrayList<HashMap<String, Object>> inspectingTableData = new ArrayList<>();
	Map<String, String> keepOnTop = new HashMap<>();

	// Activity Modes
	boolean MODE_RECORD_DATA;
	boolean MODE_INSPECT_DATA;

	// controller fields
	int numScansLeft = -1;
	private WifiAPListViewAdapter mWifiAPListViewAdapter;
	private WifiManager mWifiManager;
	private WiFiReceiver mWifiReceiver = new WiFiReceiver();
	private DataBaseManager dbManager;
	private Runnable scanner = new Runnable() {
		@Override
		public void run() {
			mWifiManager.startScan();
		}
	};
	private Comparator<WiFiAccessPoint> comparator = new Comparator<WiFiAccessPoint>() {
		@Override
		public int compare(WiFiAccessPoint lhs, WiFiAccessPoint rhs) {
			if (!lhs.isOnTop && rhs.isOnTop)
				return 1;

			if (lhs.isOnTop && !rhs.isOnTop)
				return -1;

			return 0;
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// set activity theme to light text
		this.setTheme(R.style.RecorderActivity);

		// initial setup
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_wifi_data);

		// get views and services
		mWifiApListView = (ListView) findViewById(R.id.scanned_APs);
		recordDataTableInput = (EditText) findViewById(R.id.database_table_name);
		mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		dbManager = DataBaseManager.getInstance(getApplicationContext());

		mWifiAPListViewAdapter = new WifiAPListViewAdapter(getApplicationContext());
		mWifiApListView.setAdapter(mWifiAPListViewAdapter);

		// used for sorting listview items, when clicked the list view item will get put on top, and vice-versa
		mWifiApListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String key = mWifiAPListViewAdapter.getItem(position).BSSID;
				if (keepOnTop.containsKey(key))
					keepOnTop.remove(key);
				else keepOnTop.put(key, "");

				// by doing this we refresh the list view right away
				// so that the keepOnTop items get updated instantly
				// rather than having to wait for the next wifi scan to finish
				manageData(mWifiManager.getScanResults());
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();

		// reset
		MODE_RECORD_DATA = false;
		MODE_INSPECT_DATA = false;

		setActivityMode();

		inspectingTable = null;
		inspectingTableData.clear();
		keepOnTop.clear();

		manageData(mWifiManager.getScanResults());

		registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		mHandler.postDelayed(scanner, 0);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// pause recorder
		unregisterReceiver(mWifiReceiver);
		mHandler.removeCallbacks(scanner);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_record_wifi_data, menu);

		// assign menu buttons
		MenuItem recordButton = menu.getItem(0);
		MenuItem listButton = menu.getItem(1);

		recordButton.setIcon(MODE_RECORD_DATA ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp);
		recordButton.setTitle(this.getString(MODE_RECORD_DATA ? R.string.menu_action_record_pause : R.string.menu_action_record_play));
		recordButton.setEnabled(!MODE_INSPECT_DATA);

		listButton.setIcon(MODE_INSPECT_DATA ? R.drawable.ic_clear_white_24dp : R.drawable.ic_sort_white_24dp);
		listButton.setTitle(getString(MODE_INSPECT_DATA ? R.string.menu_action_cancel : R.string.menu_action_list_tables));
		listButton.setEnabled(!MODE_RECORD_DATA);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		// record button click
		if (id == R.id.record) {

			MODE_RECORD_DATA = !MODE_RECORD_DATA;
			numScansLeft = TOTAL_SCANS;
			setActivityMode();

			return true;
		}

		// list tables button click
		if (id == R.id.list_tables) {

			if (!MODE_INSPECT_DATA) {

				// show tables dialog
				ArrayList<String> dataTables = dbManager.getDataTables();
				final CharSequence tables[] = dataTables.toArray(new CharSequence[dataTables.size()]);

				// create dialog
				new AlertDialog.Builder(this)
						.setTitle(getString(R.string.choose_data_table))
						.setItems(tables, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {

								MODE_INSPECT_DATA = true;

								inspectingTableData.clear();
								inspectingTable = tables[which].toString();

								setActivityMode();

							}
						}).show();

			} else {
				MODE_INSPECT_DATA = false;
				inspectingTable = null;
				setActivityMode();
			}

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void setActivityMode() {

		Log.i(TAG, "setting top menu");

		if (MODE_INSPECT_DATA) {
			//
			Log.i(TAG, "inspect data is ON");
			recordDataTableInput.setEnabled(false);
			recordDataTableInput.setText(inspectingTable);
		}

		if (MODE_RECORD_DATA) {
			//
			Log.i(TAG, "record data is ON");
			recordDataTableInput.setEnabled(false);
		}

		if (!MODE_INSPECT_DATA && !MODE_RECORD_DATA) {
			//
			Log.i(TAG, "none of the modes are ON");

			getSupportActionBar().setTitle(getString(R.string.recorder_activity_title));

			recordDataTableInput.setText("");
			recordDataTableInput.setEnabled(true);
		}


		// set menu
		invalidateOptionsMenu();
	}

	private void manageData(List<ScanResult> data) {

		if (MODE_INSPECT_DATA) {
			inspectTableData(mWifiManager.getScanResults());
			return;
		}

		// cache record so we don't abrupt data recording
		// in the middle of the loop if the global variable changes
		boolean localRecordDataFlag = MODE_RECORD_DATA;

		// cache table name to avoid name changing during data
		String cachedTableName = recordDataTableInput.getText().toString();

		// set activity header
		if (localRecordDataFlag) {
			getSupportActionBar().setTitle(numScansLeft + "");
			numScansLeft--;

			// no more scans left
			if (numScansLeft == 0) {
				MODE_RECORD_DATA = false;
				setActivityMode();
			}
		}


		mWifiAPListViewAdapter.clear();

		for (ScanResult res : data) {

			WiFiAccessPoint point = new WiFiAccessPoint(res.SSID,
					res.BSSID,
					res.level,
					res.frequency,
					System.currentTimeMillis(),
					null,
					null,
					keepOnTop.containsKey(res.BSSID));

			mWifiAPListViewAdapter.add(point);

			if (localRecordDataFlag)
				dbManager.addAccessPoint(point, cachedTableName);
		}

		mWifiAPListViewAdapter.sort(comparator);
		mWifiAPListViewAdapter.notifyDataSetChanged();
	}

	private void inspectTableData(List<ScanResult> scannedData) {

		mWifiAPListViewAdapter.clear();

		if (inspectingTableData.isEmpty()) {
			// get data
			inspectingTableData = dbManager.getTableData(inspectingTable);

			// clean data
			MapTools.removeDups(inspectingTableData);

			// remove unnecessary networks
			for (Iterator<HashMap<String, Object>> it = inspectingTableData.iterator(); it.hasNext(); ) {
				if (!AppConfig.ALL_SSIDS.contains(it.next().get(Keys.KEY_SSID).toString()))
					it.remove();
			}
			// remove aps based on rssi
			for (Iterator<HashMap<String, Object>> it = inspectingTableData.iterator(); it.hasNext(); ) {
				if (Integer.parseInt(it.next().get(Keys.KEY_RSSI).toString()) < -65)
					it.remove();
			}
		}


		for (HashMap<String, Object> row : inspectingTableData) {
			for (ScanResult res : scannedData) {
				if (row.get(Keys.KEY_BSSID).equals(res.BSSID)) {
					mWifiAPListViewAdapter.add(
							new WiFiAccessPoint(res.SSID,
									res.BSSID,
									res.level,
									res.frequency,
									System.currentTimeMillis(),
									Math.abs(Integer.parseInt(row.get(Keys.KEY_RSSI).toString()) - res.level),
									Integer.parseInt(row.get(Keys.KEY_RSSI).toString()),
									keepOnTop.containsKey(res.BSSID)));
				}
			}
		}

		mWifiAPListViewAdapter.sort(comparator);
		mWifiAPListViewAdapter.notifyDataSetChanged();
	}

	private class WiFiReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			manageData(mWifiManager.getScanResults());
			mHandler.postDelayed(scanner, 0);
		}
	}

}
