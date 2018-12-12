package com.implementhit.OptimizeHIT.adapter.viewholders;

import android.view.View;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;

/**
 * Created by victor on 7/26/16.
 */

public class SectionHeaderViewHolder extends SuperViewHolder {
    private TextView sectionTitle;
    private SectionHeaderProvider sectionHeaderProvider;

    public SectionHeaderViewHolder(View itemView, SectionHeaderProvider sectionHeaderProvider) {
        super(itemView);

        this.sectionHeaderProvider = sectionHeaderProvider;
        sectionTitle = (TextView) itemView.findViewById(R.id.section_title);
    }

    @Override
    public void processPosition(int position) {
        String header = sectionHeaderProvider.sectionHeaderForPosition(position);
        sectionTitle.setText(header);
    }

    public interface SectionHeaderProvider {
        String sectionHeaderForPosition(int position);
    }

}
