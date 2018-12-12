package com.implementhit.OptimizeHIT.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.util.FontsHelper;
import com.implementhit.OptimizeHIT.models.IDCRecord;

public class ICDCodesListAdapter extends ArrayAdapter<IDCRecord>{
	private  LayoutInflater inflater;
	private Context context;
	private int resource;
	private IDCRecord[] records;
	
	public  ICDCodesListAdapter(Context context, int resource, IDCRecord[] records) {
		super(context, resource, records);
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.resource = resource;
		this.records = records;
		this.context = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null ) {
			
			convertView = inflater.inflate(resource, parent, false);
			
			TextView icon = (TextView) convertView.findViewById(R.id.permission_icon);
			icon.setTypeface(FontsHelper.sharedHelper(context).fontello());
		}
		
		String code = records[position].getCode();
		
		if (code.length() > 3) {
			code = code.substring(0, 3) + "." + code.substring(3, code.length());
		}
		
		TextView itemTitle = (TextView)convertView.findViewById(R.id.title);
		TextView itemSubtitle = (TextView)convertView.findViewById(R.id.subtitle);
		itemTitle.setText(records[position].getDescription());
		itemSubtitle.setText(code);
		
		return convertView;
	}
	
	@Override
	public int getCount() {
		return records.length;
	}

	@Override
	public IDCRecord getItem(int position) {
		return records[position];
	}

	public IDCRecord[] getRecords() {
		return records;
	}

	public void setRecords(IDCRecord[] records) {
		this.records = records;
		notifyDataSetChanged();
	}
}
