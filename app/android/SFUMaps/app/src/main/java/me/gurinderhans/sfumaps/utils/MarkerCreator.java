package me.gurinderhans.sfumaps.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.PointF;
import android.graphics.drawable.PictureDrawable;
import android.util.Pair;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.larvalabs.svgandroid.SVGBuilder;

import java.util.ArrayList;
import java.util.List;

import me.gurinderhans.sfumaps.BuildConfig;
import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.factory.classes.MapPlace;

/**
 * Created by ghans on 15-09-01.
 */
public class MarkerCreator {

	public static Bitmap createPlaceIcon(Context c, MapPlace place, MapLabelIconAlign iconAlign) {
		Bitmap markerText = createTextIcon(c, place.getTitle(), null);

		Integer rId = place.getType().getResourceId();
		if (rId == null) { // text icon only
			if (markerText == null)
				return pictureDrawableToBitmap(new SVGBuilder().readFromResource(c.getResources(), R.drawable.location_marker)
						.build().getPicture());
			return markerText;
		}

		// ELSE: add icon

		// FIXME: 15-09-05 decide if resource image will be SVG or PNG
		Bitmap markerIcon = pictureDrawableToBitmap(new SVGBuilder().readFromResource(c.getResources(), rId)
				.build().getPicture());

		// combine text and image
		if (markerText != null)
			markerIcon = combineLabelBitmaps(markerIcon, markerText, iconAlign);

		return markerIcon;
	}

	public static Marker createPlaceMarker(Context c, GoogleMap map, MapPlace place) {

		MapLabelIconAlign imageIconAlignment = place.getIconAlignment();
		PointF labelAnchor = place.getIconAlignment().getAnchorPoint();

		return map.addMarker(new MarkerOptions()
						.position(MercatorProjection.fromPointToLatLng(place.getPosition()))
						.icon(BitmapDescriptorFactory.fromBitmap(createPlaceIcon(c, place, imageIconAlignment)))
						.anchor(labelAnchor.x, labelAnchor.y)
						.rotation(place.getMarkerRotation())
						.draggable(BuildConfig.DEBUG)
						.flat(place.getType() == MapPlaceType.ROAD)
						.visible(BuildConfig.DEBUG)
		);
	}


	/* Helper functions */

	private static Bitmap createTextIcon(Context c, String text, Pair<Integer, Integer> rotation) {

		if (text == null || text.isEmpty())
			return null;

		IconGenerator generator = new IconGenerator(c);
		generator.setBackground(null);
		generator.setTextAppearance(R.style.MapTextRawStyle);
		generator.setContentPadding(0, 0, 0, 0);

		if (rotation != null) {
			generator.setRotation(rotation.first);
			generator.setContentRotation(rotation.second);
		}

		return generator.makeIcon(text);
	}

	private static Bitmap combineLabelBitmaps(Bitmap icon, Bitmap text, MapLabelIconAlign alignment) {

		if (alignment == MapLabelIconAlign.T) {
			Bitmap bmp = Bitmap.createBitmap(text.getWidth(), icon.getHeight() + text.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bmp);
			canvas.drawBitmap(icon, text.getWidth() / 2f - (icon.getWidth() / 2), 0f, null);
			canvas.drawBitmap(text, 0, icon.getHeight(), null);
			return bmp;
		} else if (alignment == MapLabelIconAlign.B) {

			Bitmap bmp = Bitmap.createBitmap(text.getWidth(), icon.getHeight() + text.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bmp);
			canvas.drawBitmap(text, 0, 0, null);

			// +10 to add extra space so the icon isn't merged into the text
			canvas.drawBitmap(icon, text.getWidth() / 2f - (icon.getWidth() / 2), icon.getHeight() + 10, null);
			return bmp;
		}

		int width = icon.getWidth() + text.getWidth() + 5; // extra padding of 5
		int height = (alignment == MapLabelIconAlign.L) ? icon.getHeight() : text.getHeight();

		Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);

		if (alignment == MapLabelIconAlign.L) {
			canvas.drawBitmap(icon, 0f, 0f, null);
			canvas.drawBitmap(text, icon.getWidth() + 5, -6f, null);
		} else if (alignment == MapLabelIconAlign.R) {
			canvas.drawBitmap(text, 0f, 0f, null);
			canvas.drawBitmap(icon, text.getWidth() + 5, 6f, null);
		}


		return bmp;
	}

	private static Bitmap pictureDrawableToBitmap(Picture picture) {
		PictureDrawable pd = new PictureDrawable(picture);
		Bitmap bitmap = Bitmap.createBitmap(pd.getIntrinsicWidth(), pd.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawPicture(pd.getPicture());
		return bitmap;
	}

	// enum for placing label icon on which side
	public enum MapLabelIconAlign {
		T("Top", new PointF(0.5f, 0.5f)),
		L("Left", new PointF(0f, 1f)),
		R("Right", new PointF(1f, 1f)),
		B("Bottom", new PointF(0.5f, 0.5f));

		private String text;
		private PointF anchorPoint;

		MapLabelIconAlign(String text, PointF anchorPoint) {
			this.text = text;
			this.anchorPoint = anchorPoint;
		}

		public String getText() {
			return this.text;
		}

		public PointF getAnchorPoint() {
			return anchorPoint;
		}

		public static MapLabelIconAlign fromString(String text) {
			if (text != null)
				for (MapLabelIconAlign align : MapLabelIconAlign.values())
					if (text.equalsIgnoreCase(align.text))
						return align;

			return T;
		}

		public static List<String> allValues() {
			List<String> values = new ArrayList<>();

			for (MapLabelIconAlign align : MapLabelIconAlign.values())
				values.add(align.getText());

			return values;
		}
	}

	// enum for marker place types
	public enum MapPlaceType {
		ROOM("Room", null),
		ROOM_LG("Room (Large)", R.drawable.location_marker),
		ROAD("Road", null),
		BLDG("Building", R.drawable.location_marker),
		SPECIAL("Special", R.drawable.location_marker);

		private String text;
		private Integer resourceId;

		MapPlaceType(String text, Integer rId) {
			this.text = text;
			this.resourceId = rId;
		}

		public String getText() {
			return this.text;
		}

		public Integer getResourceId() {
			return resourceId;
		}

		public static MapPlaceType fromString(String text) {
			if (text != null)
				for (MapPlaceType align : MapPlaceType.values())
					if (text.equalsIgnoreCase(align.text))
						return align;

			return ROOM;
		}

		public static List<String> allValues() {
			List<String> values = new ArrayList<>();

			for (MapPlaceType align : MapPlaceType.values())
				values.add(align.getText());

			return values;
		}
	}
}
