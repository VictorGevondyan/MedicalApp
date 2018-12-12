package com.implementhit.OptimizeHIT.models;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Notification implements Serializable {
	private static final long serialVersionUID = 2627898065292126663L;

	private final static String ID = "id";
	private final static String TEXT = "notification";
	private final static String SOLUTION_ID = "solution_id";
	private final static String CATEGORY_ID = "category_id";
	private final static String SUB_CATEGORY_ID = "subcategory_id";
	private final static String PAGE_TO_OPEN = "pageToOpen";
	private final static String SUGGESTION = "sug";
	private final static String TIMESTAMP = "timestamp";
	private final static String READ = "read";

	private int notificationId;
	private String notificationText;
	private String correspondSolutionId; //

	private int correspondCategoryId; //
	private int correspondSubCategoryId; //
	private int pageToOpen; //

	private boolean opensSuggestion; //
	private long notificationReceiveDate;
	private boolean read;

	public Notification(JSONObject notificationJSONObject){
		notificationId = notificationJSONObject.optInt(ID);
		notificationText = notificationJSONObject.optString(TEXT);
		correspondSolutionId = notificationJSONObject.optString(SOLUTION_ID);
		notificationReceiveDate = notificationJSONObject.optLong(TIMESTAMP);
		read = notificationJSONObject.optString(READ).equals("1");

        correspondCategoryId = -1;
        if (notificationJSONObject.optString(CATEGORY_ID) instanceof String) {
        	try {
        		correspondCategoryId = Integer.valueOf(notificationJSONObject.optString(CATEGORY_ID));
        	} catch (Exception e) {
        		correspondCategoryId = -1;
        	}
        } else {
        	correspondCategoryId = notificationJSONObject.optInt(CATEGORY_ID, -1);
        }

        correspondSubCategoryId = -1;
        if (notificationJSONObject.optString(SUB_CATEGORY_ID) != null) {
        	try {
        		correspondSubCategoryId = Integer.valueOf(notificationJSONObject.optString(SUB_CATEGORY_ID));
	    	} catch (Exception e) {
	    		correspondCategoryId = -1;
	    	}
        } else {
        	correspondSubCategoryId = notificationJSONObject.optInt(SUB_CATEGORY_ID, -1);
        }

		pageToOpen = -1;
		if (notificationJSONObject.optString(PAGE_TO_OPEN) != null) {
			try {
				pageToOpen = Integer.valueOf(notificationJSONObject.optString(PAGE_TO_OPEN));
			} catch (Exception e) {
				pageToOpen = -1;
			}
		} else {
			pageToOpen = notificationJSONObject.optInt(PAGE_TO_OPEN, -1);
		}

		opensSuggestion = false;
		if (notificationJSONObject.optString(SUGGESTION) != null) {
			try {
				opensSuggestion = notificationJSONObject.optString(SUGGESTION, "").equals("1");
			} catch (Exception e) {
				opensSuggestion = false;
			}
		} else {
			opensSuggestion = notificationJSONObject.optInt(SUGGESTION, -1) == 1;
		}

		checkSolutionId();
	}

	public Notification(int notificationId, String notificationText, String correspondSolutionId,
			int correspondCategoryId, int correspondSubCategoryId, int pageToOpen, boolean opensSuggestion,
			long notificationReceiveDate, boolean read) {
		this.notificationId = notificationId;
		this.notificationText = notificationText;
		this.correspondSolutionId = correspondSolutionId;
		this.correspondCategoryId = correspondCategoryId;
		this.correspondSubCategoryId = correspondSubCategoryId;
        this.pageToOpen = pageToOpen;
		this.opensSuggestion = opensSuggestion;
		this.notificationReceiveDate = notificationReceiveDate;
		this.read = read;

		checkSolutionId();
	}

	public void checkSolutionId() {
		try {
			if (correspondSolutionId != null
					&& Integer.parseInt(correspondSolutionId) < 0) {
				correspondSolutionId = "";
			}
		} catch(NumberFormatException e) {
			correspondSolutionId = "";
		}
	}

	public int getNotificationId() {
		return notificationId;
	}

	public String getNotificationText() {
		return notificationText;
	}

	public String getCorrespondSolutionId() {
		return correspondSolutionId;
	}

	public int getCorrespondCategoryId() {
		return correspondCategoryId;
	}

	public int getCorrespondSubCategoryId() {
		return correspondSubCategoryId;
	}

	public long getNotificationDate() {
		return notificationReceiveDate;
	}

	public boolean isRead() {
		return read;
	}

	public boolean getOpensSuggestion() {
		return opensSuggestion;
	}

	public static String getFormattedDate( long timestamp ){
		DateFormat df = new SimpleDateFormat("dd/M/yyyy h:m a");
		Date date = new Date(timestamp*1000);
		String formattedDate =  df.format(date);
		return formattedDate;
	}

	public int getPageToOpen() {
		return pageToOpen;
	}

	public void setNotificationId( int notificationId ) {
		this.notificationId = notificationId;
	}

	public void setNotificationText( String notificationText ) {
		this.notificationText = notificationText;
	}

	public void setCorrespondSolutionId( String correspondSolutionId ) {
		this.correspondSolutionId = correspondSolutionId ;
	}

	public void setNotificationDate( long notificationReceiveDate ) {
		this.notificationReceiveDate = notificationReceiveDate;
	}

	public void setReadStatus( boolean read ) {
		this.read = read;
	}

	public void setPageToOpen(int pageToOpen) {
		this.pageToOpen = pageToOpen;
	}

}
