package com.implementhit.OptimizeHIT.models;

import android.content.Context;

import com.implementhit.OptimizeHIT.database.DBTalker;

public class Settings {
	private float speechSpeed = 1;
	private int defaultScreen = 2;
	private boolean isAutoStartSpeech = false;
	private boolean isDisplayingEnableGrouping = true;
	private boolean isGroupingEnabled = false;
	
	public Settings(int defaultScreen, float speechSpeed,
			boolean isAutoStartSpeech, boolean isDisplayingEnableGrouping, boolean isGroupingEnabled) {
		this.defaultScreen = defaultScreen;
		this.speechSpeed = speechSpeed;
		this.isAutoStartSpeech = isAutoStartSpeech;
		this.isDisplayingEnableGrouping = isDisplayingEnableGrouping;
		this.isGroupingEnabled = isGroupingEnabled;
	}

	public float speechSpeed() {
		return speechSpeed;
	}
	
	public int defaultScreen() {
		return defaultScreen;
	}
	
	public boolean isAutoStartSpeech() {
		return isAutoStartSpeech;
	}
	
	public boolean isDisplayingEnableGrouping() {
		return isDisplayingEnableGrouping;
	}
	
	public boolean isGroupingEnabled() {
		return isGroupingEnabled;
	}
	
	public void setDefaultScreen(int defaultScreen, Context context) {
		this.defaultScreen = defaultScreen;
		saveSettings(context);
	}
	
	public void setSpeechSpeed(float speechSpeed, Context context) {
		this.speechSpeed = speechSpeed;
		saveSettings(context);
	}
	
	public void setIsAutoStartSpeech(boolean isAutoStartSpeech, Context context) {
		this.isAutoStartSpeech = isAutoStartSpeech;
		saveSettings(context);
	}
	
	public void setIsGroupingEnabled(boolean isGroupingEnabled, Context context) {
		this.isGroupingEnabled = isGroupingEnabled;
		saveSettings(context);
	}

	public void setIsDisplayingEnableGrouping(boolean isDisplayingEnableGrouping, Context context) {
		this.isDisplayingEnableGrouping = isDisplayingEnableGrouping;
		saveSettings(context);
	}
	
	private void saveSettings(Context context) {
		DBTalker
			.sharedDB(context)
			.saveSettings(
					this);
	}
}
