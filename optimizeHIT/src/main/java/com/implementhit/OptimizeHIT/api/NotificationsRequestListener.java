package com.implementhit.OptimizeHIT.api;

import com.implementhit.OptimizeHIT.models.Notification;

import java.util.ArrayList;


public interface NotificationsRequestListener {
	void notificationsSuccess( ArrayList<Notification> notifications, Notification immidiateNotification );
	void notificationsFailure(String error);
}
