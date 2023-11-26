package dte.masteriot.mdp.citywanderer.RecyclerView;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import dte.masteriot.mdp.citywanderer.ListOfMonuments.DataPair;

public class Dataset {

    // This dataset is a list of Items
    private static final String TAG = "DATASET";
    private List<Item> listofitems;

    public Dataset(ArrayList<DataPair> monumentList) {
        Log.d(TAG, "Dataset() called, namesList = " + monumentList.toString());
        listofitems = new ArrayList<>();
        int position = 0;

        for (DataPair data : monumentList) {
            String name = data.getName();
            String url = data.getImageUrl();
            listofitems.add(new Item(name, url , (long) position, convertToValidTopic(name)));
            position++;

        }

    }

    public static String convertToValidTopic(String input) {
        // Convert to lowercase, remove spaces, and append "/concurrency"
        return "monuments/" + input.toLowerCase().replaceAll("\\s", "") + "/concurrency";
    }

    public int getSize() {
        return listofitems.size();
    }

    public Item getItemAtPosition(int pos) {
        return listofitems.get(pos);
    }

    Long getKeyAtPosition(int pos) {
        return (listofitems.get(pos).getKey());
    }

    public int getPositionOfKey(Long searchedkey) {
        // Look for the position of the Item with key = searchedkey.
        // The following works because in Item, the method "equals" is overriden to compare only keys:
        int position = listofitems.indexOf(new Item("placeholder","url", searchedkey, "placeholder"));
        //Log.d(TAG, "getPositionOfKey() called for key " + searchedkey + ", returns " + position);
        return position;
    }

    public void setNewData(ArrayList<DataPair> newList) {
        Log.d(TAG, "Dataset() called, namesList = " + newList.toString());
        listofitems = new ArrayList<>();
        int position = 0;

        for (DataPair data : newList) {
            String name = data.getName();
            String url = data.getImageUrl();
            listofitems.add(new Item(name, url , (long) position, convertToValidTopic(name)));
            position++;

        }

    }

    void removeItemAtPosition(int i) {
        listofitems.remove(i);
    }

    void removeItemWithKey(Long key) {
        removeItemAtPosition(getPositionOfKey(key));
    }

}
