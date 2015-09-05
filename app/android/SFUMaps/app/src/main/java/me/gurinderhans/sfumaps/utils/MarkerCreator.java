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

	public static Bitmap createPlaceIcon(Context c, MapPlace place, MapLabelIconAlign imageIconAlignment) {
		Bitmap textIcon = createTextIcon(c, place.getTitle(), Pair.create(0, 0));

		// get passed in icon or use the default one
		int iconId = R.drawable.location_marker;

		Bitmap markerIcon = pictureDrawableToBitmap(new SVGBuilder().readFromResource(c.getResources(), iconId)
				.build().getPicture());

		// combine text and image
		if (textIcon != null)
			markerIcon = combineLabelBitmaps(markerIcon, textIcon, imageIconAlignment);

		return markerIcon;
	}

	public static Marker createPlaceMarker(Context c, GoogleMap map, MapPlace place) {

		MapLabelIconAlign imageIconAlignment = MapLabelIconAlign.TOP;
		Pair<Float, Float> labelAnchor;

		switch (imageIconAlignment) {
			case LEFT:
				labelAnchor = new Pair<>(0f, 1f);
				break;

			case RIGHT:
				labelAnchor = new Pair<>(1f, 1f);
				break;

			case TOP:
				labelAnchor = new Pair<>(0.5f, 0.5f);
				break;

			default:
				labelAnchor = new Pair<>(0f, 0f);
				break;

		}

		// add icon image on actual point
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

	private static Bitmap combineLabelBitmaps(Bitmap a, Bitmap b, MapLabelIconAlign alignment) {

		if (alignment == MapLabelIconAlign.TOP) {
			Bitmap bmp = Bitmap.createBitmap(b.getWidth(), a.getHeight() + b.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bmp);
			canvas.drawBitmap(a, b.getWidth() / 2f - (a.getWidth() / 2), 0f, null);
			canvas.drawBitmap(b, 0, a.getHeight(), null);
			return bmp;
		}

		int width = a.getWidth() + b.getWidth() + 5; // extra padding of 5
		int height = (alignment == MapLabelIconAlign.LEFT) ? a.getHeight() : b.getHeight();

		Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);

		if (alignment == MapLabelIconAlign.LEFT) {
			canvas.drawBitmap(a, 0f, 0f, null);
			canvas.drawBitmap(b, a.getWidth() + 5, -6f, null);
		} else if (alignment == MapLabelIconAlign.RIGHT) {
			canvas.drawBitmap(b, 0f, 0f, null);
			canvas.drawBitmap(a, b.getWidth() + 5, 6f, null);
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
		TOP, LEFT, RIGHT
	}
}
