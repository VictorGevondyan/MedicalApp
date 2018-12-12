package com.implementhit.OptimizeHIT.api;

public interface FeedbackRequestListener {
	void leaveFeedackSuccess();
	void leaveFeedbackFailure(String error);
}
