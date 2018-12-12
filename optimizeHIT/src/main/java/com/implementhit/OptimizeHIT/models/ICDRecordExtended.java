package com.implementhit.OptimizeHIT.models;

public class ICDRecordExtended extends IDCRecord {
	private static final long serialVersionUID = 2271386467483140423L;
	private int order;
	private String group;

	public ICDRecordExtended(String code, String description, boolean billable, int order, String group) {
		super(code, description, billable);
		
		this.order = order;
		this.group = group;
	}

	public int getOrder() {
		return order;
	}

	public String getGroup() {
		return group;
	}

}
