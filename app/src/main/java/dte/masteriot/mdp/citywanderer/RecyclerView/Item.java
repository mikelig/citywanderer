package dte.masteriot.mdp.citywanderer.RecyclerView;

import android.util.Log;

import java.util.ArrayList;

import dte.masteriot.mdp.citywanderer.MonumentInfo.Point;


public class Item {
    // This class contains the actual data of each item of the dataset
    private static final String TAG = "DATASET";
    private String title;
    private Long key; // In this app we use keys of type Long
    private String mqttTopic;
    private ArrayList<Point> pointList;
    static int MAXSIZE = 10;

    public Item(String title, Long key, String topic) {
        Log.d(TAG, "Item to be created. Title = " + title + " key = " + Long.toString(key));
        this.title = title;
        this.key = key;
        this.mqttTopic = topic;
        this.pointList = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public String getTopic() { return mqttTopic;}

    public Long getKey() {
        return key;
    }

    // We override the "equals" operator to only compare keys
    // (useful when searching for the position of a specific key in a list of Items):
    public boolean equals(Object other) {
        return this.key == ((Item) other).getKey();
    }

    public void printPointListToLog() {
        for (Point p : pointList) {
            Log.d("POINT", p.toString());
        }
    }

    public void addPoint(double timestamp, int concurrency) {
        Point newPoint = new Point(timestamp, concurrency); // (x,y)

        if (pointList.size() >= MAXSIZE) {
            // If the list has reached its maximum size, remove the oldest point
            pointList.remove(0);
        }

        // Add the new point to the list
        pointList.add(newPoint);
        Log.d("POINT", "Added point: " + newPoint.toString());
    }

}