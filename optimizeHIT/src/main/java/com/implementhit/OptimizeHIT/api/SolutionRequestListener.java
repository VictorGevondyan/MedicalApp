package com.implementhit.OptimizeHIT.api;

public interface SolutionRequestListener {
	void solutionSuccess(int solutionId, String title, String html, String speech);
	void solutionFailure(String error);
}
