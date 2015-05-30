package me.gurinderhans.sfumaps.wifirecorder.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.wifirecorder.Model.WiFiAccessPoint;

/**
 * Created by ghans on 15-05-28.
 */
public class WifiAPListViewAdapter extends ArrayAdapter<WiFiAccessPoint> {

    public static final String TAG = WifiAPListViewAdapter.class.getSimpleName();

    // member variables
    Context context;

    public WifiAPListViewAdapter(Context ctx) {
        super(ctx, R.layout.lv_item_wifi_ap);
        this.context = ctx;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // get the access point
        WiFiAccessPoint accessPoint = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        WiFiAccessPointHolder accessPointHolder; // view lookup cache stored in tag
        if (convertView == null) {

            // create a new holder
            accessPointHolder = new WiFiAccessPointHolder();

            // get layout inflater
            LayoutInflater inflater = LayoutInflater.from(context);

            // fill convert view
            convertView = inflater.inflate(R.layout.lv_item_wifi_ap, parent, false);

            // get view holder's views
            accessPointHolder.ssid = (TextView) convertView.findViewById(R.id.ssid);
            accessPointHolder.bssid = (TextView) convertView.findViewById(R.id.bssid);
            accessPointHolder.rssi = (TextView) convertView.findViewById(R.id.rssi);
            accessPointHolder.rssiDiff = (TextView) convertView.findViewById(R.id.rssiDiff);

            // assign holder
            convertView.setTag(accessPointHolder);
        } else {
            // get already made holder
            accessPointHolder = (WiFiAccessPointHolder) convertView.getTag();
        }

        // populate the holder row view
        accessPointHolder.ssid.setText(accessPoint.SSID);
        accessPointHolder.bssid.setText(accessPoint.BSSID);
        accessPointHolder.rssi.setText(accessPoint.RSSI + "");

        if (accessPoint.RSSI_DIFF != null) {
            accessPointHolder.rssiDiff.setText(accessPoint.RECORDED_RSSI + " | " + accessPoint.RSSI + " = " + accessPoint.RSSI_DIFF);
        } else accessPointHolder.rssiDiff.setText("");

        if (accessPoint.isOnTop != null && accessPoint.isOnTop) {
            accessPointHolder.rssi.setBackgroundColor(context.getResources().getColor(R.color.selected_access_point));
        } else {
            accessPointHolder.rssi.setBackgroundColor(context.getResources().getColor(R.color.recorder_activity_color_dark));
        }

        // return the completed view to render on screen
        return convertView;
    }

    // lookup cache
    private static class WiFiAccessPointHolder {
        TextView ssid;
        TextView bssid;
        TextView rssi;
        TextView rssiDiff;
    }
}
