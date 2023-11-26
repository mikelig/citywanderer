package dte.masteriot.mdp.citywanderer.RecyclerView;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.widget.RecyclerView;

public class MyItemKeyProvider extends ItemKeyProvider<Long> {

    private static final String TAG = "TAGListOfItems, MyItemKeyProvider";
    RecyclerView recView;

    /**
     * Creates a new provider with the given scope.
     *
     * @param scope Scope can't be changed at runtime.
     */
    @SuppressLint("LongLogTag")
    public MyItemKeyProvider(int scope, RecyclerView rv) {
        super(scope);
        recView = rv;
        Log.d(TAG, "MyItemKeyProvider() called");
    }

    @SuppressLint("LongLogTag")
    @Nullable
    @Override
    public Long getKey(int position) {
        Log.d(TAG, "getKey() called for position " + position);
        return (((MyAdapter) recView.getAdapter()).getKeyAtPosition(position));
    }

    @SuppressLint("LongLogTag")
    @Override
    public int getPosition(@NonNull Long key) {
        Log.d(TAG, "getPosition() called for key " + key);
        return (((MyAdapter) recView.getAdapter()).getPositionOfKey(key));
    }
}
