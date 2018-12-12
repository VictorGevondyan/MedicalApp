package com.implementhit.OptimizeHIT.api;

import java.util.ArrayList;

public interface MarkNotificationsReadRequestListener {
	void onMarkNotificationsReadSuccess( ArrayList<String> notificationsIdArray );
	void onMarkNotificationsReadFail(String error);
}
