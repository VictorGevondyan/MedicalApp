package com.implementhit.OptimizeHIT.api;

public interface SessionEstablishRequestListener {
	void onLoginSuccess(String username, String firstName, String lastName, String domain, String domainLabel, String hash, long trigger, boolean voiceAccess, boolean watsonAccess, boolean findACode);
	void onLoginFailure(String error);

	void onSessionVerificationSuccess(long trigger, boolean voiceAccess, boolean watsonAccess, boolean findACode);
	void onSessionVerificationFailure(String error);
}
