package com.implementhit.OptimizeHIT.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ICD10InfoRelated extends ICD9Related {
	private String name;
	private boolean billable;

	public ICD10InfoRelated(String name, String description, String code, boolean billable) {
		super(code, description);
		
		this.name = name;
		this.billable = billable;
	}
	
	public ICD10InfoRelated(Parcel parcel) {

		super(parcel);
		
		this.name = parcel.readString();
		this.billable = parcel.readInt() == 1;

	}
	
	public String getName() {
		return name;
	}
	
	public boolean getBillable() {
		return billable;
	}
	
    public static final Parcelable.Creator<ICD10InfoRelated> CREATOR
		    = new Parcelable.Creator<ICD10InfoRelated>() {

		public ICD10InfoRelated createFromParcel(Parcel in) {
		    return new ICD10InfoRelated(in);
		}
		
		public ICD10InfoRelated[] newArray(int size) {
		    return new ICD10InfoRelated[size];
		}

    };
    		
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);;
		
		dest.writeString(name);
		dest.writeInt(billable ? 1 : 0);
	}
}
