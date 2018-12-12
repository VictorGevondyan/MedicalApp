package com.implementhit.OptimizeHIT.api;

import android.content.Context;

public interface PushNotificationAPIRequestListener {
	void pushTokenUpdateSuccess(Context context, String token);
	void pushTokenUpdateFail(Context context);
}
