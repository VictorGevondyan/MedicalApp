package com.implementhit.OptimizeHIT.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;

public class PickerAdapter extends ArrayAdapter<String> {
	private LayoutInflater inflater;
	private String[] data;

	public PickerAdapter(Context context, String[] data) {
		super(context, R.layout.item_picker, data);
		
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.data = data;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_picker, parent, false);
		}
		
		TextView text = (TextView) convertView.findViewById(R.id.text);
		text.setText(getItem(position));
		
		return convertView;
	}
	
	@Override
	public int getCount() {
		return data.length + 4;
	}
	
	@Override
	public String getItem(int position) {
		if (position > 1 && position < data.length + 2) {
			return data[position - 2];
		} else {
			return "";
		}
	}
}