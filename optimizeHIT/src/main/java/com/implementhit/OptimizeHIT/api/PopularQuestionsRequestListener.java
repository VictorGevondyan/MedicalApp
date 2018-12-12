package com.implementhit.OptimizeHIT.api;

public interface PopularQuestionsRequestListener {
	void onPopularQuestionsSuccess(String[] questions);
	void onPopularQuestionsFail(String error);
}
