package me.gurinderhans.sfumaps.app;

/**
 * Created by ghans on 2/9/15.
 */
public final class Keys {

	// Keys for accessing table data
	public static final String KEY_ROWID = "_id";
	public static final String KEY_SSID = "ssid";
	public static final String KEY_BSSID = "bssid";
	public static final String KEY_FREQ = "freq"; // Use this to give priority to one AP over another
	public static final String KEY_RSSI = "level";
	public static final String KEY_TIME = "rec_time";
	public static final String KEY_POINT = "point";

	// database check keys
	public static final String KEY_REVERSED = "REVERSE";

	// SharedPrefs keys
	public static final String KEY_APP_CONFIG_PREFS = "MapConfig";

	// App Config Preferences keys
	public static final String KEY_CONFIG_RSSI_THRESHOLD = "MIN_RSSI_THRESHOLD";
	public static final String KEY_CONFIG_SSID_SET = "USABLE_SSID_SET";

	// App Hierarchy keys
	public static final String KEY_HIERARCHY_NAME = "name";
	public static final String KEY_HIERARCHY_SELF_ID = "self";
	public static final String KEY_HIERARCHY_PARENT_ID = "parent";
	public static final String KEY_HIERARCHY_VALUE = "value";


	public static class ParseMapPlace {
		/* Map MapPlace (ParseObject) keys */
		public static final String CLASS = "MapPlace";
		public static final String TITLE = "placeTitle";
		public static final String TYPE = "placeType";
		public static final String ICON_ALIGNMENT = "placeIconAlignment";
		public static final String POSITION_X = "positionX";
		public static final String POSITION_Y = "positionY";
		public static final String ZOOM = "placeZoom";
		public static final String MARKER_ROTATION = "placeMarkerRotation";
		public static final String PARENT_PLACE = "parentPlace";
	}

	public static class ParseMapPath {
		/* Map MapPath (ParseObject) keys */
		public static final String CLASS = "MapPath";
		public static final String POINT_START_X = "startX";
		public static final String POINT_START_Y = "startY";
		public static final String POINT_END_X = "endX";
		public static final String POINT_END_Y = "endY";
		public static final String ROTATION = "pathRotation";

	}

}
