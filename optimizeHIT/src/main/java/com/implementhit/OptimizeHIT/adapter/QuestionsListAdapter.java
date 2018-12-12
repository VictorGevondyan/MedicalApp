package com.implementhit.OptimizeHIT.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;

public class QuestionsListAdapter implements View.OnClickListener {
	private Context context;
	private String[] questions;
	private LinearLayout container;
	private OnQuestionClickedListener listener;
	
	public QuestionsListAdapter(Context context, String[] questions, LinearLayout container, OnQuestionClickedListener listener) {
		this.context = context;
		this.questions = questions;
		this.container = container;
		this.listener = listener;
	}
	
	public void setItems(String[] questions) {
		this.questions = questions;

		container.removeAllViews();

		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		for (String question : questions) {
			View questionView = layoutInflater.inflate(R.layout.item_question, container, false);
			TextView questionTitle = (TextView) questionView.findViewById(R.id.question);
			questionTitle.setText(question);
			container.addView(questionView);

			questionView.setOnClickListener(this);
		}

		container.requestLayout();
	}

	public String getItem(int position) {
		return questions[position];
	}

	@Override
	public void onClick(View view) {
		int childCount = container.getChildCount();

		for (int index = 0 ; index < childCount ; index++) {
			if (view.equals(container.getChildAt(index))) {
				listener.onQuestionClicked(questions[index]);
			}
		}
	}

	public interface OnQuestionClickedListener {
		void onQuestionClicked(String question);
	}
}
