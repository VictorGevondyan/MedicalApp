package com.implementhit.OptimizeHIT.factory;

import org.json.JSONArray;
import org.json.JSONObject;

import com.implementhit.OptimizeHIT.models.ICD10InfoRelated;
import com.implementhit.OptimizeHIT.models.ICDAdditionalInfo;

public class ICDAdditionalInfoFactory {
	public static ICDAdditionalInfo[] getAdditionalInfo(JSONObject additinalInfo) {
		final String CONDITIONS = "conditions";
		final String NAME = "name";
		final String CLINICAL_EXAMPLE = "clinical_example";
		final String CASE_ICD10_CODES = "case_icd_10_codes";
		final String DOCUMENTATION_POINTS = "documentation_points";
		final String RELEVANT_DOCUMENTATION = "relevant_documentation";
		
		JSONArray conditionsArray = additinalInfo.optJSONArray(CONDITIONS);
		ICDAdditionalInfo[] additionalInfo = new ICDAdditionalInfo[0];
		
		if (conditionsArray != null) {
			additionalInfo = new ICDAdditionalInfo[conditionsArray.length()];
			
			for (int index = 0 ; index < conditionsArray.length() ; index++) {
				JSONObject infoRecord = conditionsArray.optJSONObject(index);
				String name = infoRecord.optString(NAME);
				String clinicalExample = infoRecord.optString(CLINICAL_EXAMPLE);
				
				JSONArray caseICD10Codes = infoRecord.optJSONArray(CASE_ICD10_CODES);
				ICD10InfoRelated[] caseRecords = new ICD10InfoRelated[0];
				
				if (caseICD10Codes != null) {
					caseRecords = getRelatedICD10(caseICD10Codes);
				}
				
				JSONArray docPointsJSON = infoRecord.optJSONArray(DOCUMENTATION_POINTS);
				String[] docPoints = new String[0];
				
				if (docPointsJSON != null) {
					docPoints = getDocumentation(docPointsJSON);
				}
				
				JSONArray relevantDocJSON = infoRecord.optJSONArray(RELEVANT_DOCUMENTATION);
				String[] relevantDoc = new String[0];
				
				if (relevantDocJSON != null) {
					relevantDoc = getDocumentation(relevantDocJSON);
				}
				
				additionalInfo[index] = new ICDAdditionalInfo(name, clinicalExample,
						caseRecords, docPoints, relevantDoc);
			}
		}
		
		return additionalInfo;
	}
	
	private static ICD10InfoRelated[] getRelatedICD10(JSONArray icd10Json) {
		final String CODE = "code";
		final String NAME = "name";
		final String BILLABLE = "billable";
		final String DESCRIPTION = "description";
		
		ICD10InfoRelated[] records = new ICD10InfoRelated[icd10Json.length()];
		
		for (int index = 0 ; index < icd10Json.length() ; index++) {
			JSONObject icdRecordJson = icd10Json.optJSONObject(index);
			records[index] = new ICD10InfoRelated(
					icdRecordJson.optString(NAME),
					icdRecordJson.optString(DESCRIPTION),
					icdRecordJson.optString(CODE),
					icdRecordJson.optBoolean(BILLABLE));
		}
		
		return records;
	}
	
	private static String[] getDocumentation(JSONArray docJson) {
		String[] documentation = new String[docJson.length()];
		
		for (int index = 0 ; index < docJson.length() ; index++) {
			documentation[index] = docJson.optString(index);
		}
		
		return documentation;
	}
}
