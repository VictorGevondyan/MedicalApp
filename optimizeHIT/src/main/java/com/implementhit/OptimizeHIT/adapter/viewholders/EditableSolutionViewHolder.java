package com.implementhit.OptimizeHIT.adapter.viewholders;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.listeners.OnItemRemovedListener;
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
public class EditableSolutionViewHolder extends SuperViewHolder implements View.OnClickListener {
    private SolutionProvider provider;
    private OnItemViewClickListener clickListener;
    private OnItemRemovedListener removedListener;
    private EditableLogicProvider editableLogicProvider;

    private TextView nameTextView;
    private TextView categoryTextView;
    private View ratingAndCategoryLayout;
    private View ratingLayout;
    private TextView ratingIconTextView;
    private TextView ratingTextView;
    private TextView cmeTextView;
    private Button editButton;
    private Button deleteButton;

    private static int deleteWidth;
    private static int editWidth;
    private static int cmeX;

    private Context context;

    public EditableSolutionViewHolder(View itemView, Context context, SolutionProvider provider, OnItemViewClickListener clickListener,
                                      OnItemRemovedListener removedListener, EditableLogicProvider editableLogicProvider) {
        super(itemView);

        itemView.setClickable(true);
        itemView.setOnClickListener(this);

        this.provider = provider;
        this.clickListener = clickListener;
        this.removedListener = removedListener;
        this.editableLogicProvider = editableLogicProvider;

        nameTextView = (TextView) itemView.findViewById(R.id.text);
        categoryTextView = (TextView) itemView.findViewById(R.id.category);
        ratingAndCategoryLayout = itemView.findViewById(R.id.rating_and_category);
        ratingLayout = itemView.findViewById(R.id.rating_layout);
        ratingLayout.setBackground(ColorUtil.getTintedDrawable(context, R.drawable.rounded_rect_grey, User.sharedUser(context).primaryColor()));
        ratingIconTextView = (TextView) itemView.findViewById(R.id.rating_icon);
        ratingTextView = (TextView) itemView.findViewById(R.id.rating);
        cmeTextView = (TextView) itemView.findViewById(R.id.cme);
        DimensionUtil.changeBackgroundPreservingPadding(cmeTextView, context, R.drawable.little_rounded_rect_orange, User.sharedUser(context).primaryColor());
        cmeTextView.requestLayout();

        editButton = (Button) itemView.findViewById(R.id.edit);
        deleteButton = (Button) itemView.findViewById(R.id.delete);

        Typeface fontelloTypeface = FontsHelper.sharedHelper(context).fontello();

        editButton.setTypeface(fontelloTypeface);
        ratingIconTextView.setTypeface(fontelloTypeface);

        editButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);

        this.context = context;
    }

    @Override
    public void processPosition(int position) {

        Solution solution = provider.solutionForPosition(position);

        nameTextView.setText(solution.title());

        String solutionCategoryName = solution.categoryName();

        View categoryContainer = (View) categoryTextView.getParent();

        if (solutionCategoryName != null) {
            categoryTextView.setText(solutionCategoryName);
            categoryContainer.setVisibility(View.VISIBLE);
        } else {
            categoryContainer.setVisibility(View.GONE);
        }

        boolean solutionHasCME = solution.hasCME();
        if (solutionHasCME) {
            cmeTextView.setVisibility(View.VISIBLE);
        } else {
            cmeTextView.setVisibility(View.GONE);
        }

        float solutionRating = solution.rating();
        boolean solutionIsRatedByUser = solution.isRatedByUser();
        boolean mustShowRatingBadge = (solutionRating != -1) && (solutionIsRatedByUser || (solutionRating != 0.0));

        if (mustShowRatingBadge) {
            ratingLayout.setVisibility(View.VISIBLE);
            String ratingString = String.valueOf(solution.formattedRating());
            ratingTextView.setText(ratingString);

            if (solutionIsRatedByUser) {
                ratingLayout.setBackground(ColorUtil.getTintedDrawable(context, R.drawable.little_rounded_rect_orange, User.sharedUser(context).primaryColor()));
                ratingIconTextView.setTextColor(context.getResources().getColor(R.color.text_white));
                ratingTextView.setTextColor(context.getResources().getColor(R.color.text_white));
            } else {
                ratingLayout.setBackground(context.getResources().getDrawable(R.drawable.rounded_rect_grey));
                ratingTextView.setTextColor(context.getResources().getColor(R.color.text_black));
            }
        } else {
            ratingLayout.setVisibility(View.GONE);
        }

        if( editableLogicProvider == null ) {
            return;
        }

        if (editableLogicProvider.isCurrentlyEditing()) {
            editButton.setVisibility(View.VISIBLE);
            editButton.setX(0);
            nameTextView.setX(editWidth);
            ratingAndCategoryLayout.setX(editWidth);
        } else {
            editButton.setVisibility(View.INVISIBLE);
            nameTextView.setX(0);
            ratingAndCategoryLayout.setX(0);
        }

        if (editableLogicProvider.isDeleteShownForPosition(position)) {
            editButton.setX(-deleteWidth);
            nameTextView.setX(editWidth - deleteWidth);
            ratingAndCategoryLayout.setX(editWidth - deleteWidth);

            if (deleteButton.getVisibility() == View.INVISIBLE) {
                cmeTextView.setX(deleteButton.getX() - deleteWidth);
            }

            deleteButton.setVisibility(View.VISIBLE);
        } else {
            deleteButton.setVisibility(View.INVISIBLE);
            editButton.setX(0);

            if (editableLogicProvider.isCurrentlyEditing()) {
                nameTextView.setX(editWidth);
                ratingAndCategoryLayout.setX(editWidth);
            } else {
                nameTextView.setX(0);
                ratingAndCategoryLayout.setX(0);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.equals(itemView)) {
            if (deleteButton.getVisibility() == View.VISIBLE) {
                hideDeleteButton(false);
            } else if (!editableLogicProvider.isCurrentlyEditing()) {
                clickListener.onItemViewClick(itemView, this);
            }
        } else if (view.getId() == R.id.edit) {

            editableLogicProvider.hidePreviousDelete( getAdapterPosition() );

            if (deleteButton.getAnimation() != null) {
                deleteButton.getAnimation().cancel();
            }

            if (deleteButton.getVisibility() == View.VISIBLE) {
                hideDeleteButton(false);
            } else {
                showDeleteButton();
            }


        } else if (view.getId() == R.id.delete) {
            removedListener.onItemRemoved(itemView, this);
        }
    }

    private void showDeleteButton() {
        final int finalWidth = deleteButton.getMeasuredWidth() > 0 ? deleteButton.getMeasuredWidth() : deleteWidth;
        final int initialTextX = (int) nameTextView.getX();
        cmeX = (int) cmeTextView.getX();

        if (finalWidth > 0) {
            deleteWidth = finalWidth;
        }

        ViewGroup.LayoutParams layoutParams = deleteButton.getLayoutParams();
        layoutParams.width = 1;
        deleteButton.setLayoutParams(layoutParams);
        deleteButton.requestLayout();
        deleteButton.setVisibility(View.VISIBLE);

        editableLogicProvider.deleteShownForPosition(getAdapterPosition());

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int width = (int) (interpolatedTime * finalWidth);
                ViewGroup.LayoutParams layoutParams = deleteButton.getLayoutParams();
                layoutParams.width = width;
                deleteButton.setLayoutParams(layoutParams);
                deleteButton.requestLayout();

                editButton.setX(-width);
                nameTextView.setX(initialTextX - width);
                ratingAndCategoryLayout.setX(initialTextX - width);
                cmeTextView.setX(cmeX - width);
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration(500);
        deleteButton.startAnimation(animation);
    }

    public void hideDeleteButton(final boolean finishEdit) {
        final int currentWidth = deleteButton.getMeasuredWidth();
        final int initialTextX = (int) nameTextView.getX();
        final int initialCMEX = (int) cmeTextView.getX();

        editableLogicProvider.deleteHidden();

//        deleteButton.setVisibility(View.VISIBLE);
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int collapseWidth = (int) (interpolatedTime * currentWidth);
                ViewGroup.LayoutParams layoutParams = deleteButton.getLayoutParams();
                layoutParams.width = currentWidth - collapseWidth;
                deleteButton.setLayoutParams(layoutParams);
                deleteButton.requestLayout();

                editButton.setX(-(currentWidth - collapseWidth));
                nameTextView.setX(initialTextX + collapseWidth);
                ratingAndCategoryLayout.setX(initialTextX + collapseWidth);
                cmeTextView.setX(initialCMEX + collapseWidth);

                if (interpolatedTime >= 1.0) {
                    deleteButton.setVisibility(View.INVISIBLE);

                    if (finishEdit) {
                        hideEditButton();
                    }
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration(finishEdit ? 100 : 500);
        deleteButton.startAnimation(animation);
    }

    public void showEditButton() {
        if (editButton.getAnimation() != null) {
            editButton.getAnimation().cancel();
            editButton.setAnimation(null);
        }

        final int finalWidth = editButton.getMeasuredWidth();
        final int initialContainerX = (int) nameTextView.getX();
        final int initialContainerWidth = nameTextView.getMeasuredWidth();

        final int initialRatingAndCategoryX = (int) ratingAndCategoryLayout.getX();
        final int initialRatingAndCategoryWidth = ratingAndCategoryLayout.getMeasuredWidth();

        editWidth = editButton.getMeasuredWidth();

        editButton.setX(-finalWidth);
        editButton.setVisibility(View.VISIBLE);

        Animation animation = new Animation() {

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {

                int width = (int) (interpolatedTime * finalWidth);
                editButton.setX(width - finalWidth);

                nameTextView.setX(initialContainerX + width);

                ViewGroup.LayoutParams containerLayoutParams = nameTextView.getLayoutParams();
                containerLayoutParams.width = initialContainerWidth - width;
                nameTextView.setLayoutParams(containerLayoutParams);
                nameTextView.requestLayout();

                ratingAndCategoryLayout.setX(initialRatingAndCategoryX + width);

                ViewGroup.LayoutParams ratingAndCategoryLayoutParams = ratingAndCategoryLayout.getLayoutParams();
                containerLayoutParams.width = initialRatingAndCategoryWidth - width;
                ratingAndCategoryLayout.setLayoutParams(ratingAndCategoryLayoutParams);
                ratingAndCategoryLayout.requestLayout();


            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }

        };

        animation.setDuration(250);
        editButton.startAnimation(animation);
    }

    public void hideEditButton() {
        if (editButton.getAnimation() != null) {
            editButton.getAnimation().cancel();
            editButton.setAnimation(null);
        }

        if (deleteButton.getVisibility() == View.VISIBLE) {
            hideDeleteButton(true);
            return;
        }

        final int initialWidth = editButton.getMeasuredWidth() - 1;
        final int initialContainerX = (int) nameTextView.getX();
        final int initialContainerWidth = nameTextView.getMeasuredWidth();

        final int initialRatingAndCategoryX = (int) ratingAndCategoryLayout.getX();
        final int initialRatingAndCategoryWidth = ratingAndCategoryLayout.getMeasuredWidth();

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int width = (int) (interpolatedTime * initialWidth);
                editButton.setX(-width);

                nameTextView.setX(initialContainerX - width);

                ViewGroup.LayoutParams containerLayoutParams = nameTextView.getLayoutParams();
                containerLayoutParams.width = initialContainerWidth + width;
                nameTextView.setLayoutParams(containerLayoutParams);
                nameTextView.requestLayout();

                ratingAndCategoryLayout.setX(initialRatingAndCategoryX - width);

                ViewGroup.LayoutParams ratingAndCategoryLayoutParams = ratingAndCategoryLayout.getLayoutParams();
                containerLayoutParams.width = initialRatingAndCategoryWidth + width;
                ratingAndCategoryLayout.setLayoutParams(ratingAndCategoryLayoutParams);
                ratingAndCategoryLayout.requestLayout();

            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration(250);
        editButton.startAnimation(animation);
    }

    public interface EditableLogicProvider {

        boolean isCurrentlyEditing();

        boolean isDeleteShownForPosition(int position);

        void deleteShownForPosition(int position);

        void deleteHidden();

        void hidePreviousDelete( int currentDeletePosition );

    }

}