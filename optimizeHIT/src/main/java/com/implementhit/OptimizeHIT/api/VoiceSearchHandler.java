package com.implementhit.OptimizeHIT.api;

import com.implementhit.OptimizeHIT.models.Solution;

import java.util.ArrayList;

public interface VoiceSearchHandler {
	void voiceSearchSuccess(ArrayList<Solution> solutions, int requestId);
	void voiceSearchSuccess(Solution solution, String html, String speech, int requestId);
	void voiceSearchFailure(String error, int requestId);
}