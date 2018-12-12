package com.implementhit.OptimizeHIT.api;

import com.implementhit.OptimizeHIT.models.ICD9Related;
import com.implementhit.OptimizeHIT.models.ICDAdditionalInfo;

public interface ExploreSuperbillRequestListener {
	void exploreSuperbillSuccess(ICD9Related[] idc9Related,
			ICDAdditionalInfo[] icd10Info, boolean billable, String code, String description);
	void exploreSuperbillFailure(String error);
}
