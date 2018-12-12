package com.implementhit.OptimizeHIT.api;

public interface ChangePasswordRequestListener {
	void changePasswordSuccess(String error, String message);
	void changePasswordFail(String error, String message);
}
