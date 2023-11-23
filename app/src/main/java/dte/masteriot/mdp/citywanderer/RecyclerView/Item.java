package dte.masteriot.mdp.citywanderer.RecyclerView;

import android.util.Log;

public class Item {
    // This class contains the actual data of each item of the dataset
    private static final String TAG = "DATASET";
    private String title;
    private Long key; // In this app we use keys of type Long
    private String mqttTopic;

    public Item(String title, Long key, String topic) {
        Log.d(TAG, "Item to be created. Title = " + title + " key = " + Long.toString(key));
        this.title = title;
        this.key = key;
        this.mqttTopic = topic;
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



}