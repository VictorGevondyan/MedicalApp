package com.implementhit.OptimizeHIT.api;

public interface UpdateLocationHandler {
	void onLocationUpdateSuccess();
	void onLocationUpdateFail(String error);
}