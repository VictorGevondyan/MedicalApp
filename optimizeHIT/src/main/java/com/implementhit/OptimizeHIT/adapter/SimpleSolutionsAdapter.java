package com.implementhit.OptimizeHIT.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.listeners.OnItemViewClickListener;
import com.implementhit.OptimizeHIT.adapter.listeners.OnSolutionItemClickListener;
import com.implementhit.OptimizeHIT.adapter.providers.SolutionProvider;
import com.implementhit.OptimizeHIT.adapter.viewholders.SimpleSolutionViewHolder;
import com.implementhit.OptimizeHIT.adapter.viewholders.SuperViewHolder;
import com.implementhit.OptimizeHIT.models.Solution;

import java.util.ArrayList;

/**
 * Created by victor on 7/27/16.
 */
public class SimpleSolutionsAdapter extends RecyclerView.Adapter<SuperViewHolder> implements OnItemViewClickListener, SolutionProvider {

    private LayoutInflater inflater;
    private RecyclerView recyclerView;
    private Context context;
    private ArrayList<Solution> solutions;
    private OnSolutionItemClickListener solutionItemClickListener;

    /**
     *  Adapter's methods
     */

    public SimpleSolutionsAdapter(Context context, ArrayList<Solution> solutions) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.solutions = solutions;
        this.context = context;
    }

    public ArrayList<Solution> getSolutions() {
        return solutions;
    }

    public void setSolutions( ArrayList<Solution> solutions ) {
        this.solutions = solutions;
        notifyDataSetChanged();
    }

    public Solution getItem(int position) {
        return solutions.get(position);
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public void setOnSolutionItemClickListener( OnSolutionItemClickListener solutionItemClickListener ){
        this.solutionItemClickListener = solutionItemClickListener;
    }

    @Override
    public SuperViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_simple_solution, parent, false);
        return new SimpleSolutionViewHolder(view, context, this, this);
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
        solutionItemClickListener.onSolutionItemClick( solutions.get(position), position);
    }

    @Override
    public Solution solutionForPosition(int position) {
        return getItem(position);
    }

}
