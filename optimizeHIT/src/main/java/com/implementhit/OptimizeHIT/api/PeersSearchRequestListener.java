package com.implementhit.OptimizeHIT.api;

import org.json.JSONArray;

public interface PeersSearchRequestListener {
	void peersSearchSuccess( JSONArray peerSolutions);
	void peersSearchFailure(String error);
}
