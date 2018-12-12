package com.implementhit.OptimizeHIT.models;

import java.io.Serializable;

/**
 * This class instance represent an array of data, which is intended to fill the ListView.
 * Data can be a superbill or a group ( they are joined in ICDRecordExtended idea ).
 * The class used during navigation between lists. i. e.  it used as an element of a lists navigation stack.
 * Each element of a stack represents one list data.
 */

public class NavigationModel implements Serializable {

	private static final long serialVersionUID = 0L;
	
	private String description;
	private String code;
	private ICDRecordExtended[] subordinate;
	
	public NavigationModel(String description, String code, ICDRecordExtended[] subordinate) {
		this.description = description;
		this.code = code;
		this.subordinate = subordinate;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getDescription() {
		return description;
	}
	
	public ICDRecordExtended[] getSubordinates() {
		return subordinate;
	}

}
