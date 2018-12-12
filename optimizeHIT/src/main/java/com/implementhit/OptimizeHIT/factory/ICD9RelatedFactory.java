package com.implementhit.OptimizeHIT.factory;

import org.json.JSONArray;
import org.json.JSONObject;

import com.implementhit.OptimizeHIT.models.ICD9Related;

public class ICD9RelatedFactory {
	public static ICD9Related[] getICD9RelatedRecords(JSONArray idc9RecordsJson) {
		final String CODE = "code";
		final String DESCRIPTION = "description";
		
		ICD9Related[] records = new ICD9Related[idc9RecordsJson.length()];
		
		for (int index = 0 ; index < idc9RecordsJson.length() ; index++) {
			JSONObject recordJson = idc9RecordsJson.optJSONObject(index);
			ICD9Related record = new ICD9Related(
					recordJson.optString(CODE),
					recordJson.optString(DESCRIPTION));
			records[index] = record;
		}
		
		return records;
	}
}
