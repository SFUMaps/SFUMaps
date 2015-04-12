package me.gurinderhans.sfumaps.mapsystem;

import android.graphics.PointF;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ghans on 15-04-11.
 */
public class Campus {

    String campusName;
    PointF startPoint;
    PointF endPoint;
    List<Building> buildingList;

    public Campus(String name) {
        this.campusName = name;
        this.startPoint = new PointF(0f, 0f);
        this.endPoint = new PointF(140f, 140f);

        // buildings will be pulled out of a database or something
        createBuildings(Arrays.asList("AQ", "ASB", "TASC1", "TASC2"));

    }

    void createBuildings(List<String> buildingsToCreate) {
        for (String name : buildingsToCreate)
            buildingList.add(new Building(name));
    }

    class Building {

        String mName;
        PointF startPoint;
        PointF endPoint;
        List<Street> streets;
        List<Avenue> avenues;

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