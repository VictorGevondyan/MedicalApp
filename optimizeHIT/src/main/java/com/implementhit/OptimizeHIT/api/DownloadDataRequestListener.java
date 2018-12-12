package com.implementhit.OptimizeHIT.api;


public interface DownloadDataRequestListener {
	void onDownloadSuccess();
	void onDownloadFail(String error);
}
