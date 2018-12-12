package com.implementhit.OptimizeHIT.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class IDCRecord implements Parcelable, Serializable, Comparable<IDCRecord> {
	private static final long serialVersionUID = 1L;
	
	private String code;
	private String description;
	private boolean billable;
	
	public IDCRecord(String code, String description, boolean billable) {
		this.code = code;
		this.description = description;
		this.billable = billable;
	}

	public String getCode() {
		return code;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean getBillable() {
		return billable;
	}
	
	//IMPLEMENTING PARCELABLE
	
	public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(code);
        out.writeString(description);
        out.writeByte( (byte)( billable? 1:0 ) );
    }

    public static final Parcelable.Creator<IDCRecord> CREATOR
            = new Parcelable.Creator<IDCRecord>() {
        public IDCRecord createFromParcel(Parcel in) {
            return new IDCRecord(in);
        }

        public IDCRecord[] newArray(int size) {
            return new IDCRecord[size];
        }
    };
    
    private IDCRecord(Parcel in) {
        code = in.readString();
        description = in.readString();
        billable = in.readByte() != 0;
    }
    
    /**
     * Comparable Methods
     */

	@Override
	public int compareTo(IDCRecord record) {
		return this.code.compareTo(record.code);
	}
}
