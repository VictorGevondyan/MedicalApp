package com.implementhit.OptimizeHIT.fragments;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.activity.MenuActivity;
import com.implementhit.OptimizeHIT.activity.SuperActivity;
import com.implementhit.OptimizeHIT.adapter.NotificationsListAdapter;
import com.implementhit.OptimizeHIT.adapter.listeners.OnNotificationItemClickListener;
import com.implementhit.OptimizeHIT.analytics.GAnalyticsScreenNames;
import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.api.TalkersConstants;
import com.implementhit.OptimizeHIT.database.DBTalker;
import com.implementhit.OptimizeHIT.dialogs.YesNoDialog;
import com.implementhit.OptimizeHIT.dialogs.YesNoDialog.YesNoDialogListener;
import com.implementhit.OptimizeHIT.models.Notification;
import com.implementhit.OptimizeHIT.models.Solution;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.CheckConnectionHelper;
import com.implementhit.OptimizeHIT.util.NotificationHelper;

import org.json.JSONArray;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment implements YesNoDialogListener {
	private final String LIST_STATE = "listState";
	
	private NotificationsListAdapter adapter;
	private View notificationsView;
	private RecyclerView notificationsRecyclerView;
	private NotificationActionListener listener;
	private YesNoDialog yesNoDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		OptimizeHIT.sendScreen(GAnalyticsScreenNames.NOTIFICATIONS_SCREEN, null, null, null);

		notificationsView = inflater.inflate(R.layout.fragment_notifications, container, false);
		notificationsRecyclerView = (RecyclerView) notificationsView.findViewById(R.id.notifications_list);

		adapter = new NotificationsListAdapter(getActivity(), new ArrayList<Notification>());
		adapter.setRecyclerView(notificationsRecyclerView);
		adapter.setOnNotificationItemClickListener(notificationClickListener);
		notificationsRecyclerView.setAdapter(adapter);

		RecyclerView.LayoutManager notificationsLayoutManager = new LinearLayoutManager(getActivity());
		notificationsRecyclerView.setLayoutManager(notificationsLayoutManager);

		yesNoDialog = (YesNoDialog) getActivity().getSupportFragmentManager().findFragmentByTag("yesNoDialog");
		
		if (yesNoDialog != null) {
			yesNoDialog.setHandler(this);
		}

		return notificationsView;
	}
	
	private synchronized void showHideNoNotifications() {
		int noResultVisibility;
		int checkVisibility;
		
		if (adapter.getItemCount() > 0) {
			noResultVisibility = View.GONE;
		} else {
			noResultVisibility = View.VISIBLE;
		}
		
		int unreadNotificationsCount = User.sharedUser(getActivity()).unreadNotifications();
		
		if (unreadNotificationsCount == 0) {
			checkVisibility = View.GONE;
		} else {
			checkVisibility = View.VISIBLE;
		}

		//getActivity().findViewById(R.id.right_button).setVisibility(checkVisibility);
		if (checkVisibility == 8) {
			getActivity().findViewById(R.id.right_button).setAlpha(0.5f);
            getActivity().findViewById(R.id.right_button).setClickable(false);
		} else {
			getActivity().findViewById(R.id.right_button).setAlpha(1f);
            getActivity().findViewById(R.id.right_button).setClickable(true);
		}
		notificationsView.findViewById(R.id.no_result).setVisibility(noResultVisibility);
	}

	@Override
	public void onResume() {
		super.onResume();

		reloadNotifications();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if (activity instanceof NotificationActionListener) {
			this.listener = (NotificationActionListener) activity;
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		this.listener = null;
	}
	
	// Called from MenuActivity
	public void onMarkAllNotificationsRead() {
		yesNoDialog = new YesNoDialog();
		yesNoDialog.setupDialog(
				R.string.mark_all_read_confirm_title,
				R.string.mark_all_read_confirm_message,
				R.string.cancel);
		yesNoDialog.setHandler(NotificationsFragment.this);
		yesNoDialog.show(getFragmentManager(), "yesNoDialog");
	}
	
	private void markAllNotificationsAsRead() {
		NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
		
		ArrayList<Notification> notifications = DBTalker.sharedDB(getActivity()).getNotifications();
		ArrayList<String> notificationIds = new ArrayList<>();
		JSONArray jsonArray = new JSONArray();
		String notificationIdString;

		for (int index = 0; index < notifications.size(); index++) {
			notificationIdString = String.valueOf(notifications.get(index).getNotificationId());
			notificationIds.add(notificationIdString);
			jsonArray.put(notificationIdString);
			notifications.get(index).setReadStatus(true);
		}

		String jsonFormattedString = jsonArray.toString();
		APITalker.sharedTalker().markNotificationRead(
				User.sharedUser(getActivity()).hash(), jsonFormattedString,
				null);
		DBTalker.sharedDB(getActivity()).markNotificationsReadInDb(notificationIds);
		User.sharedUser(getActivity()).setUnreadNotifications(0);
		listener.setNotificationsBadge();
		listener.hideBannerForNotification(-1, true);
		
		adapter.setItems(notifications);
		showHideNoNotifications();
	}

	public void addNotification( Notification notification ) {
		adapter.setItem(notification);
		showHideNoNotifications();
	}
	
	public void reloadNotifications() {
		if (adapter != null) {
			ArrayList<Notification> notifications = DBTalker.sharedDB(getActivity()).getNotifications();
			adapter.setItems(notifications);
			showHideNoNotifications();
		}
	}
	
	/**
	 * YesNoDialogListener Methods
	 */

	@Override
	public void onDialogAction(String actionCode) {
		if (actionCode.equals(YesNoDialog.ACTION_YES)) {
			markAllNotificationsAsRead();
		}
	}

	public void openCorrespondSolution( Notification notification ){

		if (notification.getCorrespondSolutionId().isEmpty() && notification.getCorrespondCategoryId() <= 0 &&
				notification.getCorrespondSubCategoryId() <= 0 && notification.getPageToOpen() <= 0 &&
				!notification.getOpensSuggestion()) {

            NotificationHelper.showNotification(getString(R.string.notification_text), notification.getNotificationText(), false, (SuperActivity) getActivity());

			setNotificationRead(notification);
            return;
		}

		String solutionId = notification.getCorrespondSolutionId();

		NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(notification.getNotificationId());
		
		listener.hideBannerForNotification(notification.getNotificationId(), false);
		
		if (!CheckConnectionHelper.isNetworkAvailable(getActivity())) {
			NotificationHelper.showNotification(TalkersConstants.JUST_FAILURE, (SuperActivity) getActivity());
			
			return;
		}
		
		if (solutionId != null && !solutionId.isEmpty()) {
			((MenuActivity) getActivity()).openSolution(new Solution(Integer.parseInt(solutionId), ""), APITalker.CALL_TYPES.CALL_TYPE_PUSH_NOTIFICATION);
		}

		// Check if notification has category and subcategory and if, opens corresponding screens
		( (MenuActivity)getActivity() ).proceedCategoryAndSubcategory(notification);

		setNotificationRead(notification);
	}

    private void setNotificationRead(Notification notification) {
        JSONArray jsonArray = new JSONArray();
        String notificationIdString = String.valueOf(notification.getNotificationId());
        jsonArray.put(notificationIdString);
        String jsonFormattedString = jsonArray.toString();

        APITalker.sharedTalker().markNotificationRead(
                User.sharedUser(getActivity()).hash(), jsonFormattedString,
                null);

        notification.setReadStatus(true);
        adapter.notifyDataSetChanged();

        ArrayList<String> notificationIds = new ArrayList<>();
        notificationIds.add(notificationIdString);

        DBTalker.sharedDB(getActivity()).markNotificationsReadInDb(notificationIds);
        int unreadNotificationsNumber = DBTalker.sharedDB(getActivity()).getUnreadNotificationsNumber();
        User.sharedUser(getActivity()).setUnreadNotifications(unreadNotificationsNumber);
        listener.setNotificationsBadge();
    }
	
	/**
	 * Action Listener
	 */
	
	public interface NotificationActionListener {
		public void setNotificationsBadge();
		public void hideBannerForNotification(int notificationId, boolean allRead);
	}

	OnNotificationItemClickListener notificationClickListener = new OnNotificationItemClickListener() {
		@Override
		public void onNotificationItemClick(Notification notification, int position) {
				openCorrespondSolution(notification);
		}
	};

}
























