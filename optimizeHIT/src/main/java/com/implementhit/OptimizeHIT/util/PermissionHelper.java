package com.implementhit.OptimizeHIT.util;

import java.util.List;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.support.v4.content.ContextCompat;

public class PermissionHelper {
	public static boolean checkIfHasPermission(Context context, String permission) {
		return ContextCompat.checkSelfPermission(context.getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
	}
	
	/**
	 * Microphone Availability Methods
	 */
	
	public static boolean hasMicrophoneHardware(Context context) {
		PackageManager pm = context.getPackageManager();
		List<?> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

		return activities.size() > 0
				&& pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
	}
	
	public static boolean isMicrophoneAvailable(Context context) {
		return hasMicrophoneHardware(context)
				&& checkIfHasPermission(context, Manifest.permission.RECORD_AUDIO);
	}
}
