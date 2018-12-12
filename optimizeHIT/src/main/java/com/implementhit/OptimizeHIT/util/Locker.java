package com.implementhit.OptimizeHIT.util;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.Surface;

public class Locker {

	public static void lock(Activity activity) {

		if (activity == null) {
			return;
		}

		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		if (rotation == Surface.ROTATION_0) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if (rotation == Surface.ROTATION_90) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else if (rotation == Surface.ROTATION_180) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
		} else {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		}

	}
	
	public static void unlock(Activity activity) {

		if (activity == null) {
			return;
		}
		
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

	}

}
