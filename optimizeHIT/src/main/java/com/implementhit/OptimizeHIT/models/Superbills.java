package com.implementhit.OptimizeHIT.models;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Superbills {
	private static final String SUPERBILL_PREFERENCES = "superbillPreferences";
	private static final String UNGROUPED = "ungrouped";
	private static final String BILLABLE = "billable";
	private static final String DESCRIPTION = "description";
	private static final String ORDERING = "ordering";
	private static final String GROUP = "group";
	private static final String CODE = "code";

	private Context context;
	private ArrayList<ICDRecordExtended> ungrouped;
	private String username;

	public Superbills(Context context, String username) {
		this.context = context;
		this.username = username;
		
		SharedPreferences preferences = context.getSharedPreferences(SUPERBILL_PREFERENCES, 0);

		String superbillsString = preferences.getString(UNGROUPED + username, "{}");
		JSONArray ungroupedArray;
		
		try {
			ungroupedArray = new JSONArray(superbillsString);
		} catch (JSONException e) {
			ungroupedArray = new JSONArray();
		}
		
		
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public IDCRecord[] getUngrouped() {
		return new IDCRecord[0];
	}
	
	public void addSuperbill(String description, boolean billable, String code) {
//		try {
//			JSONObject superbill = new JSONObject();
//			superbill.put(BILLABLE, billable ? "1" : "0");
//			superbill.put(DESCRIPTION, description);
//			superbill.put(ORDERING, 0);
//			superbill.put(GROUP, "ungrouped");
//			
//			ungrouped.put(code, superbill);
//			
//			APITalker.sharedTalker().addSuperbill(
//					User.sharedUser(context).hash(),
//					code,
//					description,
//					billable,
//					"ungrouped",
//					0);
//			
//			saveSuperbill();
//			
//			OptimizeHIT.sendEvent(
//					GAnalitycsEventNames.ADD_TO_SUPERBILL.CATEGORY,
//					GAnalitycsEventNames.ADD_TO_SUPERBILL.ACTION,
//					GAnalitycsEventNames.ADD_TO_SUPERBILL.LABEL);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
	}
	
	public void deleteUngrouped(String code) {
//		if (ungrouped.has(code)) {
//			ungrouped.remove(code);
//			
//			APITalker.sharedTalker().removeSuperbill(
//					User.sharedUser(context).hash(),
//					code);
//			
//			saveSuperbill();
//			
//			OptimizeHIT.sendEvent(
//					GAnalitycsEventNames.REMOVE_FROM_SUPERBILL.CATEGORY,
//					GAnalitycsEventNames.REMOVE_FROM_SUPERBILL.ACTION,
//					GAnalitycsEventNames.REMOVE_FROM_SUPERBILL.LABEL);
//		}
	}
	
	public void setSuperbill(JSONArray superbills) {
//		ungrouped = superbills;
//		saveSuperbill();
	}
	
	public boolean hasSuperbill(String code) {
//		for (int index = 0 ; index < ungrouped.length() ; index++) {
//			if (ungrouped.optJSONObject(index).optString(CODE).equals(code)) {
//				return true;
//			}
//		}
		
		return false;
	}
	
	private void saveSuperbill() {
		SharedPreferences preferences = context.getSharedPreferences(SUPERBILL_PREFERENCES, 0);
		Editor editor = preferences.edit();

		editor.putString(UNGROUPED + username, ungrouped.toString());
		editor.commit();
	}
}