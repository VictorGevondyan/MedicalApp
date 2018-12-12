package com.implementhit.OptimizeHIT.api;

import com.implementhit.OptimizeHIT.models.IDCRecord;

public interface FindCodeRequestListener {
	void findCodeSuccess(IDCRecord[] records);
	void findCodeFailure(String error);
}
