package com.implementhit.OptimizeHIT.adapter.viewholders;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by victor on 7/26/16.
 */
public class SearchGhostViewHolder extends SuperViewHolder {
    private SearchHeaderProtocol protocol;
    private View itemView;

    public SearchGhostViewHolder(View itemView, SearchHeaderProtocol protocol) {
        super(itemView);

        this.protocol = protocol;
        this.itemView = itemView;
    }

    @Override
    public void processPosition(int position) {
        if (protocol != null) {
            itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, protocol.getSearchBarHeight()));
            itemView.setAlpha(0);
        }
    }

    public interface SearchHeaderProtocol {
        int getSearchBarHeight();
    }
}
