package com.implementhit.OptimizeHIT.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ICD9Related implements Parcelable {
	private String code;
	private String description;
	
	public ICD9Related(String code, String description) {
		this.code = code;
		this.description = description;
	}
	
	public ICD9Related(Parcel parcel) {
		code = parcel.readString();
		description = parcel.readString();
	}
	
	public String getCode() {
		return code;
	}
	
	public String getDescription() {
		return description;
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
    public static final Parcelable.Creator<ICD9Related> CREATOR
	    = new Parcelable.Creator<ICD9Related>() {
		public ICD9Related createFromParcel(Parcel in) {
		    return new ICD9Related(in);
		}
		
		public ICD9Related[] newArray(int size) {
		    return new ICD9Related[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(code);
		dest.writeString(description);
	}
}
