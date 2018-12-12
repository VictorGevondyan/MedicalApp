package com.implementhit.OptimizeHIT.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.listeners.OnItemRemovedListener;
import com.implementhit.OptimizeHIT.adapter.listeners.OnItemViewClickListener;
import com.implementhit.OptimizeHIT.adapter.listeners.OnSolutionItemClickListener;
import com.implementhit.OptimizeHIT.adapter.providers.SolutionProvider;
import com.implementhit.OptimizeHIT.adapter.viewholders.EditableSolutionViewHolder;
import com.implementhit.OptimizeHIT.adapter.viewholders.OnRemoveSolutionListener;
import com.implementhit.OptimizeHIT.adapter.viewholders.SuperViewHolder;
import com.implementhit.OptimizeHIT.models.Solution;

import java.util.ArrayList;

/**
 * Created by victor on 7/27/16.
 */
public class EditableSolutionsAdapter extends RecyclerView.Adapter<SuperViewHolder> implements
        OnItemViewClickListener, SolutionProvider, OnItemRemovedListener, EditableSolutionViewHolder.EditableLogicProvider {
    private final String IS_EDITING = "isEditing";
    private final String SHOWN_DELETE_POSITION = "showDeletePosition";

    private LayoutInflater inflater;
    private RecyclerView recyclerView;
    private Context context;
    private ArrayList<Solution> solutions;

    private boolean isEditing;
    private int shownDeletePosition;
    private int previousDeletePosition;

    private OnSolutionItemClickListener solutionItemClickListener;
    private OnRemoveSolutionListener onRemoveSolutionListener;

    /**
     * Adapter's methods
     */

    public EditableSolutionsAdapter(Context context, ArrayList<Solution> solutions) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.solutions = solutions;
        this.context = context;
        this.isEditing = false;
        this.shownDeletePosition = -1;
        this.previousDeletePosition = -1;
    }

    public ArrayList<Solution> getSolutions() {
        return solutions;
    }

    public void setSolutions(ArrayList<Solution> solutions) {
        this.solutions = solutions;
        notifyDataSetChanged();
    }

    public void restoreState(Bundle savedState) {
        isEditing = savedState.getBoolean(IS_EDITING, false);
        shownDeletePosition = savedState.getInt(SHOWN_DELETE_POSITION, -1);
    }

    public void saveState(Bundle outState) {
        outState.putBoolean(IS_EDITING, isEditing);
        outState.putInt(SHOWN_DELETE_POSITION, shownDeletePosition);
    }

    public Solution getItem(int position) {
        return solutions.get(position);
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public void setOnSolutionItemClickListener(OnSolutionItemClickListener solutionItemClickListener) {
        this.solutionItemClickListener = solutionItemClickListener;
    }

    public void setOnRemoveSolutionListener(OnRemoveSolutionListener onRemoveSolutionListener) {
        this.onRemoveSolutionListener = onRemoveSolutionListener;
    }

    public void changeEditingMode() {
        isEditing = !isEditing;

        if (!isEditing) {
            shownDeletePosition = -1;
        }

        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int startPosition = layoutManager.findFirstVisibleItemPosition();
        int lastPosition = layoutManager.findLastVisibleItemPosition();

        if (startPosition < 0) {
            return;
        }

        for (int position = startPosition; position <= lastPosition; position++) {
            EditableSolutionViewHolder viewHolder = (EditableSolutionViewHolder) recyclerView.findViewHolderForAdapterPosition(position);

            if (isEditing) {
                viewHolder.showEditButton();
            } else {
                viewHolder.hideEditButton();
            }
        }

        notifyItemRangeChanged(0, startPosition);
        notifyItemRangeChanged(lastPosition + 1, solutions.size());
    }

    @Override
    public SuperViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_editable_solution, parent, false);
        return new EditableSolutionViewHolder(view, context, this, this, this, this);
    }

    @Override
    public void onBindViewHolder(SuperViewHolder holder, int position) {
        holder.processPosition(position);
    }

    @Override
    public int getItemCount() {
        return solutions.size();
    }

    @Override
    public void onItemViewClick(View itemView, SuperViewHolder viewHolder) {
        int position = recyclerView.getChildAdapterPosition(itemView);
        solutionItemClickListener.onSolutionItemClick(solutions.get(position), position);
    }

    @Override
    public Solution solutionForPosition(int position) {
        return getItem(position);
    }

    @Override
    public void onItemRemoved(View itemView, SuperViewHolder viewHolder) {
        int position = recyclerView.getChildAdapterPosition(itemView);
        onRemoveSolutionListener.onRemoveSolution(solutions.get(position));
        solutions.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean isCurrentlyEditing() {
        return isEditing;
    }

    @Override
    public boolean isDeleteShownForPosition(int position) {
        return position == shownDeletePosition;
    }

    @Override
    public void deleteShownForPosition(int position) {
        shownDeletePosition = position;
    }

    @Override
    public void deleteHidden() {
        shownDeletePosition = -1;
    }

    @Override
    public void hidePreviousDelete(int currentDeletePosition) {

        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int startPosition = layoutManager.findFirstVisibleItemPosition();
        int lastPosition = layoutManager.findLastVisibleItemPosition();

        if (startPosition < 0) {
            return;
        }

        EditableSolutionViewHolder viewHolder;
        for (int position = startPosition; position <= lastPosition; position++) {

            if (position != currentDeletePosition) {

                if( isDeleteShownForPosition(position) ) {

                    viewHolder = (EditableSolutionViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                    viewHolder.hideDeleteButton(false);

                }

            }

        }

        notifyItemRangeChanged(0, startPosition);
        notifyItemRangeChanged(lastPosition + 1, solutions.size());

    }

}
