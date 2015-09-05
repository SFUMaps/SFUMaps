package me.gurinderhans.sfumaps.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.util.Pair;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.larvalabs.svgandroid.SVGBuilder;

import me.gurinderhans.sfumaps.BuildConfig;
import me.gurinderhans.sfumaps.R;
import me.gurinderhans.sfumaps.factory.classes.MapPlace;

/**
 * Created by ghans on 15-09-01.
 */
public class MarkerCreator {

	public static Bitmap createPlaceIcon(Context c, MapPlace place, MapLabelIconAlign iconAlign) {
		Bitmap textIcon = createTextIcon(c, place.getTitle(), null);

		// get passed in icon or use the default one
		int iconId = R.drawable.location_marker;

		Bitmap markerIcon = pictureDrawableToBitmap(new SVGBuilder().readFromResource(c.getResources(), iconId)
				.build().getPicture());

		// combine text and image
		if (textIcon != null)
			markerIcon = combineLabelBitmaps(markerIcon, textIcon, iconAlign);

		return markerIcon;
	}

	public static Marker createPlaceMarker(Context c, GoogleMap map, MapPlace place) {

		MapLabelIconAlign imageIconAlignment = place.getIconAlignment();
		Pair<Float, Float> labelAnchor = place.getIconAlignment().getAnchorPoint();

		return map.addMarker(new MarkerOptions()
						.position(MercatorProjection.fromPointToLatLng(place.getPosition()))
						.icon(BitmapDescriptorFactory.fromBitmap(createPlaceIcon(c, place, imageIconAlignment)))
						.anchor(labelAnchor.first, labelAnchor.second)
						.rotation(place.getMarkerRotation())
						.draggable(BuildConfig.DEBUG)
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
		T("Top", new Pair<>(0.5f, 0.5f)),
		L("Left", new Pair<>(0f, 1f)),
		R("Right", new Pair<>(1f, 1f)),
		B("Bottom", new Pair<>(0.5f, 0.5f));

		private String text;
		private Pair<Float, Float> anchorPoint;

		MapLabelIconAlign(String text, Pair<Float, Float> anchorPoint) {
			this.text = text;
			this.anchorPoint = anchorPoint;
		}

		public String getText() {
			return this.text;
		}

		public Pair<Float, Float> getAnchorPoint() {
			return anchorPoint;
		}

		public static MapLabelIconAlign fromString(String text) {
			if (text != null)
				for (MapLabelIconAlign align : MapLabelIconAlign.values())
					if (text.equalsIgnoreCase(align.text))
						return align;

			return T;
		}
	}
}
