package com.implementhit.OptimizeHIT;

import android.net.Uri;

public class AppInfo {
	// CREDENTIALS NEEDED TO SET UP "DRAGON MOBILE SDK" SPEECH RECOGNITION TOOL
	public static final String APP_KEY = "c934379cf9ef9b909ce198490309bdd2398ab1d8604346e4083c495f0174dbcaa91366a20201329ee3cbe8e6d4a5b90d306ae590ad2b74b667bd156832f033db";
	public static final String APP_ID = "NMDPPRODUCTION_ImplementHIT__Inc__OptimizeHIT_Training_20121207075811";
	public static final String SERVER_HOST = "ef.nmdp.nuancemobility.net";
	public static final int SERVER_PORT = 443;

	public static final Uri SERVER_URI = Uri.parse("nmsps://" + APP_ID + "@" + SERVER_HOST + ":" + SERVER_PORT);
}
