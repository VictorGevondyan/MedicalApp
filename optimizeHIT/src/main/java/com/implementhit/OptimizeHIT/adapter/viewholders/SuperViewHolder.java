package com.implementhit.OptimizeHIT.adapter.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by victor on 7/26/16.
 */
public abstract class SuperViewHolder extends RecyclerView.ViewHolder {

    public SuperViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void processPosition(int position);
}