package com.implementhit.OptimizeHIT.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.activity.MenuActivity;
import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.database.DBTalker;
import com.implementhit.OptimizeHIT.models.User;

import java.util.Set;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String NOTIFICATION_ACTION = "notificationAction";
    public static final String EXTRA_NOTIFICATION_ID = "i";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_SOLUTION_ID = "solution_id";
    public static final String EXTRA_CATEGORY_ID = "c";
    public static final String EXTRA_SUGGESTION = "sug";
    public static final String EXTRA_SUB_CATEGORY_ID = "sc";
    public static final String EXTRA_RECEIVE_DATE = "from";
    public static final String EXTRA_READ_STATE = "read";
    public static final String EXTRA_PAGE_TO_OPEN = "page";

    private static int counter = 0;

    @Override
    public void onReceive(final Context context, Intent intent) {
        // GET EXTRAS NECESSARY FOR BUILDING NOTIFICATION OBJECT
        int notificationId ;
        Set<String> keySet =  intent.getExtras().keySet();
        String page = intent.getStringExtra("page");

        Bundle intentExtras = intent.getExtras();

        if (intentExtras.get(EXTRA_NOTIFICATION_ID) instanceof String) {
        	String notificationIdString = intent.getStringExtra(EXTRA_NOTIFICATION_ID);
        	notificationId = Integer.parseInt(notificationIdString);
        } else {
        	notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
        }

        String notificationText = intent.getStringExtra(EXTRA_MESSAGE);

        String correspondSolutionId;
        if (intentExtras.get(EXTRA_SOLUTION_ID) instanceof String) {
        	correspondSolutionId = intent.getStringExtra(EXTRA_SOLUTION_ID);
        } else {
        	int solutionIdInt = intent.getIntExtra(EXTRA_SOLUTION_ID, -1);
        	correspondSolutionId = String.valueOf(solutionIdInt);
        }

        if (correspondSolutionId.equals("-1")) {
            if (intentExtras.get("s") instanceof String) {
                correspondSolutionId = intent.getStringExtra("s");
            } else {
                int solutionIdInt = intent.getIntExtra("s", -1);
                correspondSolutionId = String.valueOf(solutionIdInt);
            }
        }

        boolean opensSuggestion;
        if (intentExtras.get("s") instanceof String) {
            try {
                opensSuggestion = intent.getExtras().getString(EXTRA_SUGGESTION, "").equals("1");
            } catch (Exception e) {
                opensSuggestion = false;
            }
        } else {
            opensSuggestion = intent.getExtras().getInt(EXTRA_SUGGESTION, -1) == 1;
        }

        int correspondCategoryId;
        if (intentExtras.get(EXTRA_CATEGORY_ID) instanceof String) {
        	try {
        		correspondCategoryId = Integer.valueOf(intent.getStringExtra(EXTRA_CATEGORY_ID));
        	} catch (Exception e) {
        		correspondCategoryId = -1;
        	}
        } else {
        	correspondCategoryId = intent.getIntExtra(EXTRA_CATEGORY_ID, -1);
        }

        int correspondSubCategoryId;
        if (intentExtras.get(EXTRA_SUB_CATEGORY_ID) instanceof String) {
        	try {
        		correspondSubCategoryId = Integer.valueOf(intent.getStringExtra(EXTRA_SUB_CATEGORY_ID));
	    	} catch (Exception e) {
	    		correspondSubCategoryId = -1;
	    	}
        } else {
        	correspondSubCategoryId = intent.getIntExtra(EXTRA_SUB_CATEGORY_ID, -1);
        }

        int pageToOpen;
        if (intentExtras.get(EXTRA_PAGE_TO_OPEN) instanceof String) {
            String pageToOpenString = intent.getStringExtra(EXTRA_PAGE_TO_OPEN);
            pageToOpen = Integer.parseInt(pageToOpenString);
        } else {
            pageToOpen = intent.getIntExtra(EXTRA_PAGE_TO_OPEN, -1);
        }

        long notificationReceiveDate = System.currentTimeMillis() / 1000;

        // HERE WE BUILD RECEIVED NOTIFICATION FROM DATA THAT WE OBTAIN ABOVE
        com.implementhit.OptimizeHIT.models.Notification receivedNotification = new
                com.implementhit.OptimizeHIT.models.Notification(
                notificationId, notificationText, correspondSolutionId,
                correspondCategoryId, correspondSubCategoryId, pageToOpen, opensSuggestion,
                notificationReceiveDate, false);

        DBTalker.sharedDB(context).insertNotification(receivedNotification);

        // WE CHECK LOG IN CONDITION HERE, AFTER INSERTING NOTIFICATION INTO DATABASE ,
        // BECAUSE EVEN IF USER IS LOGGED OUT , HE MUST RECEIVE NOTIFICATION ( BUT IT WILL
        // NOT SHOWN IMMEDIATELY, IT WILL BE SAVED INTO DATABASE ),
        // AND IN FUTURE, WHEN HE WILL LOG IN, WE WILL SHOW HIM THAT NOTIFICATION.
        if (!User.sharedUser(context).isLoggedIn()) {
            return;
        }

        if ( !OptimizeHIT.isInForeground()) {
        	notifyUser(correspondSolutionId, correspondSubCategoryId, correspondCategoryId, notificationId, opensSuggestion, context, intent);
        }

        com.implementhit.OptimizeHIT.models.Notification existingNotification = DBTalker.sharedDB(context).getNotification(notificationId);



        if ( existingNotification != null && !existingNotification.isRead() ) {
        	if (OptimizeHIT.isInForeground()) {
        		DBTalker.sharedDB(context).insertBadgeNotification(notificationId);
        	}

            counter++;

        	Intent showNotificationIntent = new Intent(APITalker.ACTION_SHOW_NOTIFICATION);
        	showNotificationIntent.putExtra(DBTalker.EXTRA_NOTIFICATION_ID, notificationId);
        	context.sendBroadcast(showNotificationIntent);
        }

        int notifications = DBTalker.sharedDB(context).getUnreadNotificationsNumber();
        User.sharedUser(context).setUnreadNotifications(notifications);

    }

    private void notifyUser(String solutionId, int subCategoryId, int categoryId, int notificationId, boolean opensSuggestion, Context context, Intent intent) {
        Intent notificationIntent = new Intent(Intent.ACTION_MAIN);
    	notificationIntent.setClass(context.getApplicationContext(), MenuActivity.class);
        notificationIntent.setAction(NOTIFICATION_ACTION);
        notificationIntent.putExtra(EXTRA_SOLUTION_ID, solutionId);
        notificationIntent.putExtra(EXTRA_SUB_CATEGORY_ID, subCategoryId);
        notificationIntent.putExtra(EXTRA_CATEGORY_ID, categoryId);
        notificationIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(EXTRA_SUGGESTION, opensSuggestion);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                (int) System.currentTimeMillis(),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String title = context.getResources().getString(R.string.app_name);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context)
            .setContentTitle(title)
            .setContentText(intent.getStringExtra(EXTRA_MESSAGE))
            .setSmallIcon(R.drawable.optiquery)
            .setContentIntent(contentIntent)
            .setTicker(title + "\n" + intent.getStringExtra(EXTRA_MESSAGE))
            .setColor(context.getResources().getColor(R.color.orange))
            .build();

        notification.flags |= NotificationCompat.FLAG_AUTO_CANCEL;
        notification.flags |= NotificationCompat.FLAG_SHOW_LIGHTS;

        notification.defaults |= NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_VIBRATE;
        notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.push);

        notificationManager.notify(notificationId, notification);
    }

}
