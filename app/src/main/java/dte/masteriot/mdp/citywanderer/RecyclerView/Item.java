package dte.masteriot.mdp.citywanderer.RecyclerView;

import android.util.Log;
import java.util.Objects;

public class Item {
    // This class contains the actual data of each item of the dataset
    private static final String TAG = "DATASET";
    private final String title;
    private final String urlImage;
    private final Long key; // In this app we use keys of type Long
    private final String mqttTopic;


    public Item(String title, String urlImage, Long key, String topic) {
        Log.d(TAG, "Item to be created. Title = " + title + " key = " + Long.toString(key));
        this.title = title;
        this.urlImage = urlImage;
        this.key = key;
        this.mqttTopic = topic;
    }

    public String getTitle() {
        return title;
    }

    public String getUrlImage() { return urlImage; }

    public String getTopic() { return mqttTopic;}

    public Long getKey() {
        return key;
    }

    // We override the "equals" operator to only compare keys
    // (useful when searching for the position of a specific key in a list of Items):
    public boolean equals(Object other) {
        return Objects.equals(this.key, ((Item) other).getKey());
    }

}