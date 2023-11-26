package dte.masteriot.mdp.citywanderer.RecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import dte.masteriot.mdp.citywanderer.R;

public class MyViewHolder extends RecyclerView.ViewHolder {

    // Holds references to individual item views
    TextView title;
    private ImageView imageView;
    private View view;

    public MyViewHolder(View itemView) {
        super(itemView);
        view = itemView;
        title = itemView.findViewById(R.id.title);
        imageView = itemView.findViewById(R.id.imageMonumentList);

    }

    void bindValues(Item item) {
        title.setText(item.getTitle());
        Glide.with(view).load(item.getUrlImage()).override(60,60).into(imageView);
    }

}