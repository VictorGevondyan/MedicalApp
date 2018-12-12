package com.implementhit.OptimizeHIT.dialogs;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.activity.SuperActivity;
import com.implementhit.OptimizeHIT.adapter.PickerAdapter;

public class PickerDialog extends ProgressDialog implements OnClickListener, OnScrollListener, OnItemClickListener {
	public static final String DATA = "data";
	public static final String TITLE = "title";
	
	private PickerAdapter adapter;
	private ListView wheelListView;
	private String selectedItem;
	private String[] data;
	private String title;
	private Context context;
	private PickerListener handler;
	
	public PickerDialog(Context context, PickerListener handler) {
		super(context);
		
		this.context = context;
		this.handler = handler;
	}

	public void setArguments(String[] data, String title) {
		this.data = data;
		this.title = title;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.dialog_picker);

		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    	setCancelable(true);
		setCanceledOnTouchOutside(false);

		TextView cancel = (TextView) findViewById(R.id.cancel);
		TextView done = (TextView) findViewById(R.id.done);
		cancel.setClickable(true);
		done.setClickable(true);
		cancel.setOnClickListener(this);
		done.setOnClickListener(this);
		
		TextView titleTextView = (TextView) findViewById(R.id.title);
		titleTextView.setText(title);

		wheelListView = (ListView) findViewById(R.id.wheel_list);
		adapter = new PickerAdapter(context, data);
		wheelListView.setAdapter(adapter);
		wheelListView.setOnScrollListener(this);
		wheelListView.setOnItemClickListener(this);
		
		selectedItem = data[0];
    }
	
	public String getPickerTitle() {
		return title;
	}
	
	/**
	 * OnClickListener Methods
	 */

	@Override
	public void onClick(View view) {
		if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
			return;
		}
		
		SuperActivity.savedLastClickTime = System.currentTimeMillis();

		if (view.getId() == R.id.cancel) {
			handler.onPickerDismissed();
			dismiss();
		} else if (view.getId() == R.id.done) {
			handler.onPick(selectedItem);
			dismiss();
		}
	}
	
	/**
	 * OnScrollListener Methods
	 */

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == SCROLL_STATE_IDLE) {
			int firstItem = wheelListView.getFirstVisiblePosition();
			int lastItem= wheelListView.getLastVisiblePosition();
			int visibleRows = lastItem - firstItem + 1;
			
			if (visibleRows == 6) {
				final int position;

				int firstViewPosition = firstItem + 2;
				int secondViewPosition = firstItem + 3;
				View firstView = wheelListView.getChildAt(2);
				View secondView = wheelListView.getChildAt(3);
				int height = secondView.getTop() - firstView.getTop();
				
				if (secondView.getTop() < firstViewPosition * height + height / 2) {
					selectedItem = data[secondViewPosition - 2];
					position = secondViewPosition - 2;
				} else {
					selectedItem = data[firstViewPosition - 2];
					position = firstViewPosition - 2;
				}
				
	        	wheelListView.setOnScrollListener(null);
				wheelListView.post(new Runnable() {
			        @Override
			        public void run() {
			        	wheelListView.setSelection(position);
			        	wheelListView.setOnScrollListener(PickerDialog.this);
			        }
			    });
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		
	}
	
	/**
	 * Listener interface
	 */
	
	public interface PickerListener {
		void onPick(String data);
		void onPickerDismissed();
	}
	
	/**
	 * OnItemClickListener Methods
	 */

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
		if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
			return;
		}
		
		SuperActivity.savedLastClickTime = System.currentTimeMillis();

		if (position > 1 && position < data.length + 2) {
			selectedItem = data[position - 2];

			wheelListView.setOnScrollListener(null);
			wheelListView.post(new Runnable() {
		        @Override
		        public void run() {
		        	wheelListView.setSelection(position - 2);
		        	wheelListView.setOnScrollListener(PickerDialog.this);
		        }
		    });
		}
	}

}
