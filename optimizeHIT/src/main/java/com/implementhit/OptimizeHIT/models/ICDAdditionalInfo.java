package com.implementhit.OptimizeHIT.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ICDAdditionalInfo implements Parcelable {
	private String name;
	private String clinicalExample;
	private ICD10InfoRelated[] icd10Records;
	private String[] documentationPoitns;
	private String[] relevantDocumentation;

	public ICDAdditionalInfo(String name, String clinicalExample,
			ICD10InfoRelated[] icd10Records, String[] documentationPoitns, String[] relevantDocumentation) {
		this.name = name;
		this.clinicalExample = clinicalExample;
		this.icd10Records = icd10Records;
		this.documentationPoitns = documentationPoitns;
		this.relevantDocumentation = relevantDocumentation;
	}
	
	public ICDAdditionalInfo(Parcel parcel) {
		this.name = parcel.readString();
		this.clinicalExample = parcel.readString();
		
		int icd10RecordsLength = parcel.readInt();
		icd10Records = new ICD10InfoRelated[icd10RecordsLength];
		parcel.readTypedArray(icd10Records, ICD10InfoRelated.CREATOR);
		
		int documentationPoitnsLength = parcel.readInt();
		documentationPoitns = new String[documentationPoitnsLength];
		parcel.readStringArray(documentationPoitns);
		
		int relevantDocumentationLength = parcel.readInt();
		relevantDocumentation = new String[relevantDocumentationLength];
		parcel.readStringArray(relevantDocumentation);
	}
	
	public String getName() {
		return name;
	}
	
	public String getClinicalExample() {
		return clinicalExample;
	}
	
	public ICD10InfoRelated[] getIcd10Records() {
		return icd10Records;
	}
	
	public String[] getDocumentationPoints() {
		return documentationPoitns;
	}
	
	public String[] getRelevantDocumentation() {
		return relevantDocumentation;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(clinicalExample);
		dest.writeInt(icd10Records.length);
		dest.writeTypedArray(icd10Records, flags);
		dest.writeInt(documentationPoitns.length);
		dest.writeStringArray(documentationPoitns);
		dest.writeInt(relevantDocumentation.length);
		dest.writeStringArray(relevantDocumentation);
	}
	
    public static final Parcelable.Creator<ICDAdditionalInfo> CREATOR
	    = new Parcelable.Creator<ICDAdditionalInfo>() {
		public ICDAdditionalInfo createFromParcel(Parcel in) {
		    return new ICDAdditionalInfo(in);
		}
		
		public ICDAdditionalInfo[] newArray(int size) {
		    return new ICDAdditionalInfo[size];
		}
	};
}
