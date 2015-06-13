package me.gurinderhans.sfumaps.Factory;

import android.graphics.PointF;
import android.util.Log;

import java.util.List;

/**
 * Created by ghans on 15-04-11.
 */
public class Campus {

    public static final String TAG = Campus.class.getSimpleName();

    String campusName;
    PointF startPoint;
    PointF endPoint;
    List<Building> buildingList;

    public Campus(String name) {
        this.campusName = name;
        this.startPoint = new PointF(58.12f, 54.5f);
        this.endPoint = new PointF(201.11f, 201.0f);
        Log.i(TAG, (endPoint.x - startPoint.x) + ", " + (endPoint.y - startPoint.y) + "");

        // buildings will be pulled out of a database or something
//        createBuildings(Arrays.asList("AQ", "ASB", "TASC1", "TASC2"));

    }

    void createBuildings(List<String> buildingsToCreate) {
        for (String name : buildingsToCreate)
            buildingList.add(new Building(name));
    }

    class Building {

        String mName;
        PointF startPoint;
        PointF endPoint;
//        List<Street> streets;
//        List<Avenue> avenues;

        public Building(String name) {
            this.mName = name;
        }

        class Level {
            public Level() {
                //
            }
        }
    }
}