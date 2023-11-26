package dte.masteriot.mdp.citywanderer.ListOfMonuments;

import java.io.Serializable;

public class DataPair  implements Comparable<DataPair>, Serializable {
    private String name;
    private String imageUrl;

    public DataPair(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public int compareTo(DataPair other) {
        return this.name.compareTo(other.name); //comparar los nombres de las DataPair
    }
}
