package com.implementhit.OptimizeHIT.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.models.ICD9Related;

public class ICD9RecordListAdapter extends ArrayAdapter<ICD9Related> {
	private  LayoutInflater inflater;
	private int resource;
	private ICD9Related[] records;
	
	public  ICD9RecordListAdapter(Context context, int resource, ICD9Related[] records) {
		super(context, resource, records);
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.resource = resource;
		this.records = records;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null ) {
			convertView = inflater.inflate(resource, parent, false);
		}
		
		TextView itemCode = (TextView)convertView.findViewById(R.id.code);
		TextView itemDescription = (TextView)convertView.findViewById(R.id.description);
		itemCode.setText(records[position].getCode());
		itemDescription.setText(records[position].getDescription());
		
		return convertView;
	}
	
	@Override
	public int getCount() {
		return records.length;
	}

	@Override
	public ICD9Related getItem(int position) {
		return records[position];
	}
}