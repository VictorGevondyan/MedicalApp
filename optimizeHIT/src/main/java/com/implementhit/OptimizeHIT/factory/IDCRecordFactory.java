package com.implementhit.OptimizeHIT.factory;

import java.util.Iterator;

import org.json.JSONObject;

import com.implementhit.OptimizeHIT.models.IDCRecord;

public class IDCRecordFactory {
	private static final String DESCRIPTION = "description";
	private static final String BILLABLE = "billable";

	public static IDCRecord[] getIDCRecords(JSONObject recordsJSON) {
		IDCRecord[] records = new IDCRecord[recordsJSON.length()];
		Iterator<?> iterator = recordsJSON.keys();
		int index = 0;
		
		while (iterator.hasNext()) {
			String code = (String) iterator.next();
			JSONObject recordJson = recordsJSON.optJSONObject(code);
			String description = recordJson.optString(DESCRIPTION, "");
			boolean billable = recordJson.optString(BILLABLE, "1").equals("1");
			
			IDCRecord record = new IDCRecord(code, description, billable);
			records[index] = record;
			index++;
		}
		
		return records;
	}
	
	public static IDCRecord[] getIDCRecords(JSONObject recordsJson, boolean billable) {
		IDCRecord[] records = new IDCRecord[recordsJson.length()];
		Iterator<?> iterator = recordsJson.keys();
		String code = null;
		String description = null;
		int index = 0;

		while (iterator.hasNext()) {
			code = (String) iterator.next();
			description = recordsJson.optString(code);
			
			IDCRecord record = new IDCRecord(code, description, billable);
			records[index] = record;
			index++;
		}
		
		return records;
	}
}
