package com.implementhit.OptimizeHIT.api;

import com.implementhit.OptimizeHIT.models.Solution;

import java.util.ArrayList;

public interface SuggestedLearningRequestListener {
	void onSuggestedLearningSuccess(ArrayList<Solution> reactivations, ArrayList<Solution> suggestions, ArrayList<Solution> locationBased, int viewedCount);
	void onSuggestedLearningFail(String error);
}
