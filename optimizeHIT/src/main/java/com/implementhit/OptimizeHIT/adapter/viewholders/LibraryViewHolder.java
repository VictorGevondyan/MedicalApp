package com.implementhit.OptimizeHIT.adapter.viewholders;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.listeners.OnItemViewClickListener;
import com.implementhit.OptimizeHIT.adapter.providers.BadgeCountProvider;
import com.implementhit.OptimizeHIT.util.FontsHelper;

/**
 * Created by victor on 7/26/16.
 */

public class LibraryViewHolder extends SuperViewHolder implements View.OnClickListener {
    private OnItemViewClickListener listener;
    private LibraryItemProvider provider;
    private BadgeCountProvider badgeCountProvider;

    private TextView iconTextView;
    private TextView titleTextView;
    private TextView arrowTextView;
    private TextView badgeTextView;

    public LibraryViewHolder(View itemView, Context context, LibraryItemProvider provider, BadgeCountProvider badgeCountProvider,
                             OnItemViewClickListener listener) {
        super(itemView);

        this.provider = provider;
        this.badgeCountProvider = badgeCountProvider;
        this.listener = listener;

        iconTextView = (TextView) itemView.findViewById(R.id.library_icon);
        titleTextView = (TextView) itemView.findViewById(R.id.text);
        arrowTextView = (TextView) itemView.findViewById(R.id.permission_icon);
        badgeTextView = (TextView) itemView.findViewById(R.id.suggested_learnings_badge);

        Typeface fontello = FontsHelper.sharedHelper(context).fontello();
        iconTextView.setTypeface(fontello);
        arrowTextView.setTypeface(fontello);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        listener.onItemViewClick(view, this);
    }

    @Override
    public void processPosition(int position) {

        String icon = provider.libraryIconForPosition(position);
        String title = provider.libraryTitleForPosition(position);
        int  unreadSuggestedLearnings = badgeCountProvider.getBadgeCount(position);

        iconTextView.setText(icon);
        titleTextView.setText(title);

        if( unreadSuggestedLearnings > 0 ){
            badgeTextView.setVisibility(View.VISIBLE);
            badgeTextView.setText( unreadSuggestedLearnings + "" );
        } else {
            badgeTextView.setVisibility(View.GONE);
        }

    }

    public interface LibraryItemProvider {
        String libraryIconForPosition(int position);
        String libraryTitleForPosition(int position);
    }

}
