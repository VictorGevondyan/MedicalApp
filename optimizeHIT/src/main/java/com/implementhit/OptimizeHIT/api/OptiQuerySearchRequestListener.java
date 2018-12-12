package com.implementhit.OptimizeHIT.api;

import com.implementhit.OptimizeHIT.models.Solution;

import java.util.ArrayList;

public interface OptiQuerySearchRequestListener {
	void onOptiQuerySuccess(ArrayList<Solution> solutions, String watsonHtml, boolean solutionValid, boolean watsonValid, int requestId);
	void onOptiQuerySuccess(Solution solution, String html, String speech, String watsonHtml, boolean solutionValid, boolean watsonValid, int requestId);
	void onOptiQueryFail(String error, int requestId);
}
