package com.implementhit.OptimizeHIT.adapter.viewholders;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.listeners.OnItemViewClickListener;
import com.implementhit.OptimizeHIT.models.Notification;
import com.implementhit.OptimizeHIT.util.FontsHelper;

/**
 * Created by victor on 7/28/16.
 */
public class NotificationsViewHolder extends SuperViewHolder implements View.OnClickListener {

    private OnItemViewClickListener itemViewClickListener;
    private NotificationItemProvider notificationItemProvider;

    private TextView notificationTitleTextView;
    private TextView notificationTimestempTextView;
    private View unreadIconView;

    public NotificationsViewHolder(View itemView, Context context, NotificationItemProvider notificationItemProvider, OnItemViewClickListener itemViewClickListener) {
        super(itemView);

        this.notificationItemProvider= notificationItemProvider;
        this.itemViewClickListener = itemViewClickListener;

        notificationTitleTextView = (TextView) itemView.findViewById(R.id.text);
        notificationTimestempTextView = (TextView) itemView.findViewById(R.id.timestemp);
        TextView notificationArrowTextView = (TextView) itemView.findViewById(R.id.permission_icon);
        unreadIconView = itemView.findViewById(R.id.unread_icon);

        Typeface fontello = FontsHelper.sharedHelper(context).fontello();
        notificationArrowTextView.setTypeface(fontello);

        itemView.setOnClickListener(this);
    }

    @Override
    public void processPosition(int position) {
        Notification notification = notificationItemProvider.notificationForPosition(position);

        notificationTitleTextView.setText(notification.getNotificationText());

        long notificationDateLong = notification.getNotificationDate();
        String notificationTimestemp = Notification.getFormattedDate(notificationDateLong);
        notificationTimestempTextView.setText(notificationTimestemp);

        unreadIconView.setVisibility(notification.isRead() ? View.INVISIBLE : View.VISIBLE);

        if (notification.getCorrespondSolutionId().isEmpty() && notification.getCorrespondCategoryId() <= 0 &&
                 notification.getCorrespondSubCategoryId() <= 0 && notification.getPageToOpen() <= 0 &&
                    !notification.getOpensSuggestion()) {
            itemView.findViewById(R.id.permission_icon).setVisibility(View.GONE);
        } else {
            itemView.findViewById(R.id.permission_icon).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
            itemViewClickListener.onItemViewClick( view, this );
    }

    public interface NotificationItemProvider {
        Notification notificationForPosition( int position );
    }

}
