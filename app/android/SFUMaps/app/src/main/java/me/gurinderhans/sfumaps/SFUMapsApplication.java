package me.gurinderhans.sfumaps;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by ghans on 15-08-28.
 */
public class SFUMapsApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// Enable Local Datastore.
		Parse.enableLocalDatastore(this);

		Parse.initialize(this, "onN8KLiec9xRevRxwcc1ojQfYPYvtnDOf4w22x1R", "RkbDDqnP8w1PcVUJfW4Ax9u2Yt09Npbu6Gl3vgWo");
	}
}
