package com.implementhit.OptimizeHIT.adapter;

import android.content.Context;
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
import com.implementhit.OptimizeHIT.adapter.viewholders.SimpleSolutionViewHolder;
import com.implementhit.OptimizeHIT.adapter.viewholders.SuperViewHolder;
import com.implementhit.OptimizeHIT.models.Solution;

import java.util.ArrayList;

/**
 * Created by victor on 1/26/17.
 */


    /**
     * Created by victor on 7/28/16.
     */
    public class FavoriteSolutionsAdapter extends RecyclerView.Adapter<SuperViewHolder> implements OnItemViewClickListener,
            SolutionProvider {

        public static final String TITLE_FAVORITE = "titleFavorite";
        public static final String TITLE_PEER_FAVORITE = "titlePeerFavorite";

        private static final int VIEW_TYPE_FAVORITE = 1;
        private static final int VIEW_TYPE_PEER_FAVORITE = 2;

        private OnSolutionItemClickListener listener;
        private LayoutInflater inflater;
        private RecyclerView recyclerView;
        private Context context;
        private ArrayList<Solution> solutions;

//        private EditableSolutionViewHolder.EditableLogicProvider editableLogicProvider;
        private OnRemoveSolutionListener onRemoveSolutionListener;

        public FavoriteSolutionsAdapter(Context context, ArrayList<Solution> solutions) {
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.solutions = solutions;
            this.context = context;
        }

        public ArrayList<Solution> getSolutions() {
            return solutions;
        }

        @Override
        public SuperViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

//            if (viewType == VIEW_TYPE_FAVORITE) {
//                View view = inflater.inflate(R.layout.item_editable_solution, parent, false);
//                return new EditableSolutionViewHolder( view, context, this, this, itemRemovedListener, null );
//            }

                View view = inflater.inflate( R.layout.item_simple_solution, parent, false );
                return new SimpleSolutionViewHolder( view, context, this, this );

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
        public int getItemViewType(int position) {

            Solution solution = solutions.get(position);

            if (solution.title().equals( TITLE_FAVORITE )) {
                return VIEW_TYPE_FAVORITE;
            }
            if (solution.title().equals( TITLE_PEER_FAVORITE )) {
                return VIEW_TYPE_PEER_FAVORITE;
            }

            return VIEW_TYPE_FAVORITE;

        }

        @Override
        public void onItemViewClick(View itemView, SuperViewHolder viewHolder) {
            int position = recyclerView.getChildAdapterPosition(itemView);
            listener.onSolutionItemClick(solutions.get(position), position);
        }

//        @Override
//        public String sectionHeaderForPosition(int position) {
//            String title = solutions.get(position).title();
//            int headerId = 0;
//
//            if (title.equals(LOCATION_BASED_HEADER)) {
//                headerId = R.string.based_on_current_location;
//            }
//            if (title.equals(TITLE_FAVORITE)) {
//                headerId = R.string.reactivation_qiuck_overview;
//            }
//            if (title.equals(TITLE_PEER_FAVORITE)) {
//                headerId = R.string.suggestions;
//            }
//
//            return context.getString(headerId);
//        }

        @Override
        public Solution solutionForPosition(int position) {
            return solutions.get(position);
        }

        public void setRecyclerView(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        public void setSolutions(ArrayList<Solution> solutions) {
            this.solutions = solutions;
            notifyDataSetChanged();
        }

        public void setOnSolutionItemClickListener(OnSolutionItemClickListener listener) {
            this.listener = listener;
        }


        public void setOnRemoveSolutionListener(OnRemoveSolutionListener onRemoveSolutionListener) {
            this.onRemoveSolutionListener = onRemoveSolutionListener;
        }

        OnItemRemovedListener itemRemovedListener = new OnItemRemovedListener() {

            @Override
            public void onItemRemoved(View itemView, SuperViewHolder viewHolder) {

                int position = recyclerView.getChildAdapterPosition(itemView);
                onRemoveSolutionListener.onRemoveSolution(solutions.get(position));
                solutions.remove(position);
                notifyItemRemoved(position);

            }

        };

    }


