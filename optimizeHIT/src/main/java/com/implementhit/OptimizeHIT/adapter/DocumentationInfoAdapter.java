package com.implementhit.OptimizeHIT.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.models.ICD10InfoRelated;
import com.implementhit.OptimizeHIT.models.ICDAdditionalInfo;

public class DocumentationInfoAdapter {
	private ICDAdditionalInfo[] icdAdditionalInfoArray;
	private boolean[] states;
	private LayoutInflater inflater;

	public DocumentationInfoAdapter(Context context, ICDAdditionalInfo[] icdAdditionalInfoArray, boolean[] states) {
		this.icdAdditionalInfoArray = icdAdditionalInfoArray;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.states = states;
	}

	public View getView(int position, View.OnClickListener listener) {
		View view = inflater.inflate(R.layout.item_documentation_guidance, null);
		
		ICDAdditionalInfo info = icdAdditionalInfoArray[position];
		TextView titleText = (TextView) view.findViewById(R.id.title);
		titleText.setText(info.getName());
		
		TextView commandText = (TextView) view.findViewById(R.id.command);
		commandText.setText(states[position] ? "(close)" : "(expand)");
		commandText.setClickable(true);
		commandText.setOnClickListener(listener);
		commandText.setTag(Integer.valueOf(position));
		
		int viewIndex = 1;
		LinearLayout guidanceContainer = (LinearLayout) view.findViewById(R.id.guidance_content);
		guidanceContainer.setVisibility(states[position] ? View.VISIBLE : View.GONE);

		String[] documentationPoints = info.getDocumentationPoints();
		
		for (int index = 0 ; index < documentationPoints.length ; index++) {
			if (documentationPoints[index] != null && documentationPoints[index].length() > 0) {
				View pointView = inflater.inflate(R.layout.item_documentation_point, null);
				TextView text = (TextView) pointView.findViewById(R.id.text);
				text.setText(documentationPoints[index]);
				guidanceContainer.addView(pointView, viewIndex);
				viewIndex++;
			}
		}
		
		viewIndex++;
		String[] relevantDocs = info.getRelevantDocumentation();
		
		for (int index = 0 ; index < relevantDocs.length ; index++) {
			if (relevantDocs[index] != null && relevantDocs[index].length() > 0) {
				View pointView = inflater.inflate(R.layout.item_documentation_point, null);
				TextView text = (TextView) pointView.findViewById(R.id.text);
				text.setText(relevantDocs[index]);
				guidanceContainer.addView(pointView, viewIndex);
				viewIndex++;
			}
		}
		
		viewIndex = viewIndex + 2;
		
		TextView clinicalExample = (TextView) guidanceContainer.findViewById(R.id.case_example);
		clinicalExample.setText(info.getClinicalExample());
		
		viewIndex++;
		ICD10InfoRelated[] related = info.getIcd10Records();
		
		for (int index = 0 ; index < related.length ; index++) {
			View pointView = inflater.inflate(R.layout.item_icd_9_record, null);
			TextView code = (TextView) pointView.findViewById(R.id.code);
			code.setText(related[index].getCode());
			TextView description = (TextView) pointView.findViewById(R.id.description);
			description.setText(related[index].getDescription());
			guidanceContainer.addView(pointView, viewIndex);
			viewIndex++;
		}

		return view;
	}
}
