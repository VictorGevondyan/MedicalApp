package com.implementhit.OptimizeHIT.gcm;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.api.PushNotificationAPIRequestListener;

import java.io.IOException;

public class GCMSubscriber {
    public static void registerForGcm(final Context context) throws IOException {
        if (!GCMUtils.checkPlayServices(context)) {
            return;
        }

        GCMUtils.gcm = GoogleCloudMessaging.getInstance(context.getApplicationContext());

        String registrationId = GCMUtils.getRegistrationId(context);
        Log.d("REGISTRATION ID: ", registrationId );

        if (registrationId.isEmpty()) {
            registerBackground(context);
        } else {
            String hash = User.sharedUser(context).hash();
            Log.d("HASH: ", hash );

            APITalker.sharedTalker().updatePushNotificationToken(User.sharedUser(context).
                    hash(), context, registrationId, handler);
        }
    }

    private static void registerBackground(final Context context) {
        new AsyncTask<Void, String, String>() {
            @Override
            protected String doInBackground(Void... asyncParams) {
                String registrationId = "";
                try {
                    GCMUtils.gcm.unregister();
                    registrationId = GCMUtils.gcm.register(GCMUtils.SENDER_ID);
                } catch (IOException e) {
                    e.printStackTrace();
                    User.sharedUser(context).setIsPushNotificationsOn(false);
                    return "wtf";
                }

                return registrationId;
            }

            @Override
            protected void onPostExecute(final String registrationId) {
                if (registrationId.equals("wtf")) {
                    return;
                }

                APITalker.sharedTalker().updatePushNotificationToken(User.sharedUser(context).
                		hash(), context, registrationId, handler);
            }
        }.execute(null, null, null);
    }

    private static PushNotificationAPIRequestListener handler = new PushNotificationAPIRequestListener() {

		@Override
		public void pushTokenUpdateSuccess(Context context, String token) {
			GCMUtils.storeRegistrationId(context, token);
			User.sharedUser(context).setIsPushNotificationsOn(true);
		}

		@Override
		public void pushTokenUpdateFail(Context context) {
			if (GCMUtils.getRegistrationId(context).isEmpty()) {
				User.sharedUser(context).setIsPushNotificationsOn(false);
			}
		}
	};
}
