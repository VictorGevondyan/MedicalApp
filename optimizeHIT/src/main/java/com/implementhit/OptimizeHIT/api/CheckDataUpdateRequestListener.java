package com.implementhit.OptimizeHIT.api;

public interface CheckDataUpdateRequestListener {
	void onCheckDataUpdateSuccess(long trigger);
	void onCheckDataUpdateFail(String error);
}
