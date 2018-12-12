package com.implementhit.OptimizeHIT.api;

public interface LikesRequestListener {
	void addLikeSuccess(String likeType);
	void addLikeFailure(String likeType, String error);

	void removeLikeSuccess(String likeType, int solutionId);
	void removeLikeFailure(String likeType, String error);
}
