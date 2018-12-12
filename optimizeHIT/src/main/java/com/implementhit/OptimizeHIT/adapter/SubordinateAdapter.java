package com.implementhit.OptimizeHIT.adapter;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.database.ICDDatabase;
import com.implementhit.OptimizeHIT.models.ICDRecordExtended;
import com.implementhit.OptimizeHIT.models.IDCRecord;
import com.implementhit.OptimizeHIT.util.FontsHelper;
import com.implementhit.OptimizeHIT.util.StringUtils;

public class SubordinateAdapter extends ArrayAdapter<ICDRecordExtended> {
	private Context context;
	private ICDRecordExtended[] subordinates;
	private SubordinateAdapterListener listener;
	private ICDDatabase icdDatabase;
	private boolean isRemovable;

	public SubordinateAdapter(Context context, ICDRecordExtended[] subordinates, SubordinateAdapterListener listener) {
		super(context, R.layout.item_question, subordinates);
		
		this.context = context;
		this.subordinates = subordinates;
		this.listener = listener;
		this.icdDatabase = ICDDatabase.sharedDatabase(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (getItem(position).getDescription() == null) {

            // If description is null, that means , that the item is a group item.
			if (convertView != null && convertView.findViewById(R.id.permission_icon) == null) {
				convertView = null;
			}
			
			return getGroupView(position, convertView, parent);

		} else {

			if (convertView != null && convertView.findViewById(R.id.front_icon) == null) {
				convertView = null;
			}

			return getSuperbillView(position, convertView, parent);
		}

	}
	
	private View getSuperbillView(int position, View convertView, ViewGroup parent) {
		TextView frontIcon = null;
		TextView actionIcon = null;

		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.item_subordinate, parent, false);
			
			frontIcon = (TextView) convertView.findViewById(R.id.front_icon);
			actionIcon = (TextView) convertView.findViewById(R.id.action_icon);
			frontIcon.setTypeface(FontsHelper.sharedHelper(context).fontello());
			actionIcon.setTypeface(FontsHelper.sharedHelper(context).fontello());
			frontIcon.setOnClickListener(frontIconClickListener);
			frontIcon.setClickable(true);
			actionIcon.setOnClickListener(actionIconClickListener);
			actionIcon.setClickable(true);
		}
		
		if (frontIcon == null) {
			frontIcon = (TextView) convertView.findViewById(R.id.front_icon);
		}
		if (actionIcon == null) {
			actionIcon = (TextView) convertView.findViewById(R.id.action_icon);
		}

		
		TextView question = (TextView) convertView.findViewById(R.id.description);
		TextView code = (TextView) convertView.findViewById(R.id.code);
		
		IDCRecord record = subordinates[position];
		
		String codeString = record.getCode();

		boolean recordIsBillable = record.getBillable();

		code.setText(StringUtils.userFriendlyICD(codeString));
		question.setText(record.getDescription());

		frontIcon.setText( recordIsBillable ? R.string.icon_dollar_circled : R.string.icon_angle_right);

		if( recordIsBillable ) {
			frontIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
		} else {
			frontIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
		}

		frontIcon.setTag(Integer.valueOf(position));
		
		if (isRemovable) {
			actionIcon.setText(R.string.icon_more);
		} else {
			if (icdDatabase.hasSuperbill(record.getCode())) {
				actionIcon.setText(R.string.icon_check);
			} else {
				actionIcon.setText(R.string.icon_plus);
			}
		}

		actionIcon.setTag(Integer.valueOf(position));
		convertView.setTag(Integer.valueOf(position));
		
		return convertView;

	}
	
	public View getGroupView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.item_group, parent, false);
			
			TextView icon = (TextView) convertView.findViewById(R.id.permission_icon);
			icon.setTypeface(FontsHelper.sharedHelper(getContext()).fontello());
		}
		
		String groupName = getItem(position).getCode();
		
		TextView group = (TextView) convertView.findViewById(R.id.group);
		group.setText(StringUtils.capitalizeWords(groupName));
		
		return convertView;

	}
	
	@Override
	public int getCount() {
		return subordinates.length;
	}
	
	@Override
	public ICDRecordExtended getItem(int position) {
		return getItemSync(position);
	}
	
	public synchronized ICDRecordExtended getItemSync(int position) {

		if (position < 0 && position >= subordinates.length) {
			return null;
		}
		
		try {
			return subordinates[position];
		} catch(ArrayIndexOutOfBoundsException e) {
			return null;
		}

	}
	
	public void setItems(ICDRecordExtended[] subordinates) {
		this.subordinates = subordinates;
		notifyDataSetChanged();
	}
	
	public void setIsRemovable(boolean isRemovable) {
		this.isRemovable = isRemovable;
	}
	
	public OnClickListener frontIconClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View view) {

			Integer position = (Integer) view.getTag();
			IDCRecord record = subordinates[position.intValue()];
			
			if (listener != null) {
				listener.onFrontNavigation(!record.getBillable(), record.getCode(), record.getDescription());
			}

		}

	};
	
	public OnClickListener actionIconClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View view) {

			Integer position = (Integer) view.getTag();
			ICDRecordExtended record = subordinates[position.intValue()];
			
			if (listener != null) {
				listener.onSubordinateAction(
						!icdDatabase.hasSuperbill(record.getCode()),
						record.getCode(),
						record.getDescription(),
						record.getBillable(),
						record.getOrder(),
						position.intValue());
			}

		}

	};
	
	public interface SubordinateAdapterListener {
		void onFrontNavigation(boolean canNavigate, String code, String description);
		void onSubordinateAction(boolean canAdd, String code, String description, boolean billable, int ordering, int position);
	}

}



































