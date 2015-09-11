package me.gurinderhans.sfumaps.app;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

import me.gurinderhans.sfumaps.factory.classes.MapPath;
import me.gurinderhans.sfumaps.factory.classes.MapPath.MapPathNode;
import me.gurinderhans.sfumaps.factory.classes.MapPlace;

/**
 * Created by ghans on 15-08-28.
 */
public class SFUMapsApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// register subclasses
		ParseObject.registerSubclass(MapPlace.class);
		ParseObject.registerSubclass(MapPath.class);
		ParseObject.registerSubclass(MapPathNode.class);

		// initialize with keys
		Parse.initialize(this, "onN8KLiec9xRevRxwcc1ojQfYPYvtnDOf4w22x1R", "RkbDDqnP8w1PcVUJfW4Ax9u2Yt09Npbu6Gl3vgWo");
	}
}
