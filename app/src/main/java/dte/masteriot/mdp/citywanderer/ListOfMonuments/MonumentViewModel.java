package dte.masteriot.mdp.citywanderer.ListOfMonuments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class MonumentViewModel extends ViewModel {
    private MutableLiveData<ArrayList<DataPair>> monumentListLiveData = new MutableLiveData<>();
    private MutableLiveData<ArrayList<DataPair>> monumentListSearchLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> searchBooleanLiveData = new MutableLiveData<>();
    private MutableLiveData<String> XMLtextLiveData = new MutableLiveData<>();

    public LiveData<ArrayList<DataPair>> getMonumentListLiveData() {
        return monumentListLiveData;
    }

    public void setMonumentList(ArrayList<DataPair> monumentList) {
        monumentListLiveData.setValue(monumentList);
    }

    public LiveData<ArrayList<DataPair>> getMonumentListSearchLiveData() {
        return monumentListSearchLiveData;
    }

    public void setMonumentListSearchLiveData(ArrayList<DataPair> monumentList) {
        monumentListSearchLiveData.setValue(monumentList);
    }

    public LiveData<Boolean> getSearchBooleanLiveData() {
        return searchBooleanLiveData;
    }

    public void setSearchBoolean(boolean yourBoolean) {
        searchBooleanLiveData.setValue(yourBoolean);
    }


    public LiveData<String> getXMLTextLiveData() {
        return XMLtextLiveData;
    }

    public void setXMLtext(String saveXML) {
        XMLtextLiveData.setValue(saveXML);
    }

}