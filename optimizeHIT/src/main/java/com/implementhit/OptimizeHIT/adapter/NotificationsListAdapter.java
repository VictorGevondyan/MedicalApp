package com.implementhit.OptimizeHIT.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.listeners.OnItemViewClickListener;
import com.implementhit.OptimizeHIT.adapter.listeners.OnNotificationItemClickListener;
import com.implementhit.OptimizeHIT.adapter.viewholders.NotificationsViewHolder;
import com.implementhit.OptimizeHIT.adapter.viewholders.SuperViewHolder;
import com.implementhit.OptimizeHIT.models.Notification;

import java.util.ArrayList;

public class NotificationsListAdapter extends RecyclerView.Adapter<NotificationsViewHolder> implements OnItemViewClickListener, NotificationsViewHolder.NotificationItemProvider {

	private Context context;
	private ArrayList<Notification> notifications;
	private RecyclerView notificationsRecyclerView;
	private OnNotificationItemClickListener notificationItemClickListener;

	private LayoutInflater inflater;

	public NotificationsListAdapter(Context context, ArrayList<Notification> notifications) {
		super();

        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
		this.notifications = notifications;
	}

	public void setItem( Notification notification ){
		notifications.add(0, notification);
		notifyDataSetChanged();
	}
	
	public void setItems(ArrayList<Notification> notifications) {
		this.notifications = notifications;
		notifyDataSetChanged();
	}

	public void setRecyclerView(RecyclerView notificationsRecyclerView) {
		this.notificationsRecyclerView = notificationsRecyclerView;
	}

	public void setOnNotificationItemClickListener( OnNotificationItemClickListener notificationItemClickListener ){
		this.notificationItemClickListener = notificationItemClickListener;
	}

	public Notification getItem( int position ){
		return notifications.get(position);
	}

	@Override
	public NotificationsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View notificationView = inflater.inflate(R.layout.item_notification, parent, false);
        return new NotificationsViewHolder( notificationView, context, this, this);
	}

	@Override
	public void onBindViewHolder(NotificationsViewHolder holder, int position) {
        holder.processPosition(position);
	}

	@Override
	public int getItemCount() {
		return notifications.size();
	}

	@Override
	public void onItemViewClick(View itemView, SuperViewHolder viewHolder) {
//		int notificationPosition = notificationsRecyclerView.getChildAdapterPosition(itemView);
		Notification clickedNotification = getItem(viewHolder.getAdapterPosition());
		notificationItemClickListener.onNotificationItemClick( clickedNotification, viewHolder.getAdapterPosition());
	}

    @Override
    public Notification notificationForPosition(int position) {
        return getItem(position);
    }

//    @Override
//    public String notificationTitleForPosition(int position) {
//        Notification notification = getItem(position);
//        String notificationTitle = notification.getNotificationText();
//        return notificationTitle;
//    }
//
//    @Override
//    public String notificationTimestempForPosition(int position) {
//        Notification notification = getItem(position);
//
//        long notificationDateLong = notification.getNotificationDate();
//
//        String notificationTimestemp = Notification.getFormattedDate(notificationDateLong);
//        return notificationTimestemp;
//    }

}

















