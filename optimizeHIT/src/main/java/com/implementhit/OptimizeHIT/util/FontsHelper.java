package com.implementhit.OptimizeHIT.util;

import android.content.Context;
import android.graphics.Typeface;

public class FontsHelper {
	private static FontsHelper fontsHelper;
	private static Typeface fontello;

	private FontsHelper(Context context) {
		fontello = Typeface.createFromAsset(context.getAssets(), "fontello.ttf");
	}
	
	public static FontsHelper sharedHelper(Context context) {
		if (fontsHelper == null) {
			fontsHelper = new FontsHelper(context);
		}
		
		return fontsHelper;
	}
	
	public Typeface fontello() {
		return fontello;
	}

}
