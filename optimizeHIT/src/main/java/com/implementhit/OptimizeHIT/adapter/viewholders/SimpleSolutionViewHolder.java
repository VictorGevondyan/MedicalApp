package com.implementhit.OptimizeHIT.adapter.viewholders;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.listeners.OnItemViewClickListener;
import com.implementhit.OptimizeHIT.adapter.providers.SolutionProvider;
import com.implementhit.OptimizeHIT.models.Solution;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.ColorUtil;
import com.implementhit.OptimizeHIT.util.DimensionUtil;
import com.implementhit.OptimizeHIT.util.FontsHelper;

/**
 * Created by victor on 7/26/16.
 */
public class SimpleSolutionViewHolder extends SuperViewHolder implements View.OnClickListener {

    private SolutionProvider provider;
    private OnItemViewClickListener listener;

    private TextView nameTextView;
    private View ratinglayout;
    private TextView ratingIconTextView;
    private TextView ratingTextView;
    private TextView timestempTextView;
    private TextView categoryTextView;
    private TextView categoryColonTextView;
    private TextView checkIconTextView;
    private TextView arrowTextView;
    private TextView cmeTextView;
    private Context context;

    public SimpleSolutionViewHolder(View itemView, Context context, SolutionProvider provider, OnItemViewClickListener listener) {
        super(itemView);

        this.provider = provider;
        this.listener = listener;
        nameTextView = (TextView) itemView.findViewById(R.id.text);
        ratinglayout = itemView.findViewById(R.id.rating_layout);
        ratinglayout.setBackground(ColorUtil.getTintedDrawable(context, R.drawable.rounded_rect_grey, User.sharedUser(context).primaryColor()));
        ratingIconTextView = (TextView) itemView.findViewById(R.id.rating_icon);
        ratingTextView = (TextView) itemView.findViewById(R.id.rating);
        timestempTextView = (TextView) itemView.findViewById(R.id.timestemp);
        categoryTextView = (TextView) itemView.findViewById(R.id.category);
        categoryColonTextView = (TextView) itemView.findViewById(R.id.category_colon);

        checkIconTextView = (TextView) itemView.findViewById(R.id.icon_check);
        arrowTextView = (TextView) itemView.findViewById(R.id.permission_icon);
        cmeTextView = (TextView) itemView.findViewById(R.id.cme);
        DimensionUtil.changeBackgroundPreservingPadding(cmeTextView, context, R.drawable.little_rounded_rect_orange, User.sharedUser(context).primaryColor());

        Typeface fontelloTypeface = FontsHelper.sharedHelper(context).fontello();

        arrowTextView.setTextColor(context.getResources().getColor(R.color.text_grey));
        arrowTextView.setTypeface(fontelloTypeface);

        ratingIconTextView.setTypeface(fontelloTypeface);
        checkIconTextView.setTypeface(fontelloTypeface);

        this.context = context;

        itemView.setOnClickListener(this);
    }

    @Override
    public void processPosition(int position) {

        Solution solution = provider.solutionForPosition(position);

        if (solution.viewed()) {
            checkIconTextView.setVisibility(View.VISIBLE);
        } else {
            checkIconTextView.setVisibility(View.GONE);
        }

        nameTextView.setText(solution.title());

        String solutionCategoryName = solution.categoryName();

        if( solutionCategoryName != null ){
            categoryTextView.setText(solutionCategoryName);
            categoryTextView.setVisibility(View.VISIBLE);
            categoryColonTextView.setVisibility(View.VISIBLE);
        } else {
            categoryTextView.setVisibility(View.GONE);
            categoryColonTextView.setVisibility(View.GONE);
        }

        boolean solutionHasCME = solution.hasCME();

        if (solutionHasCME) {
            cmeTextView.setVisibility(View.VISIBLE);
        } else {
            cmeTextView.setVisibility(View.GONE);
        }

        if (solution.isFinalSolution()) {
            arrowTextView.setVisibility(View.GONE);
        } else {
            arrowTextView.setVisibility(View.VISIBLE);
        }

        float solutionRating = solution.rating();
        boolean solutionIsRatedByUser = solution.isRatedByUser();
        boolean mustShowRatingBadge = ( solutionRating != -1 )  /*&&  ( solutionIsRatedByUser   ||  ( solutionRating != 0.0 )  )*/;

        if ( mustShowRatingBadge ){
            ratinglayout.setVisibility(View.VISIBLE);
            String ratingString = String.valueOf(solution.formattedRating());
            ratingTextView.setText(ratingString);

            if (solutionIsRatedByUser) {
                ratinglayout.setBackground(ColorUtil.getTintedDrawable(context, R.drawable.little_rounded_rect_orange, User.sharedUser(context).primaryColor()));
                ratingIconTextView.setTextColor(context.getResources().getColor(R.color.text_white));
                ratingTextView.setTextColor(context.getResources().getColor(R.color.text_white));
            } else {
                ratinglayout.setBackground(context.getResources().getDrawable(R.drawable.rounded_rect_grey));
                ratingTextView.setTextColor(context.getResources().getColor(R.color.text_black));
            }
        } else {
            ratinglayout.setVisibility(View.GONE);
        }

        if (solution.timestemp() != null) {
            timestempTextView.setVisibility(View.VISIBLE);

            String time = solution.timestemp();

            if (solution.type() == 1) {
                time = "You learned this " + time;
            }

            timestempTextView.setText(time);
        } else {
            timestempTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        listener.onItemViewClick(view, this);
    }

}


































