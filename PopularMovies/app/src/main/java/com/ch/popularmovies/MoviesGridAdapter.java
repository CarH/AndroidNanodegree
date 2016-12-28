package com.ch.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ch.popularmovies.entities.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by CarH on 04/12/2016.
 */
public class MoviesGridAdapter extends RecyclerView.Adapter<MoviesGridAdapter.ViewHolder> {
    String LOG_TAG = MoviesGridAdapter.class.getSimpleName();

    private Context mContext;
    private MovieCallback mFatherActivity;
    private List<Movie> mMovies;
    private int selectedItemPosition;

    public MoviesGridAdapter(Context context, MovieCallback fatherActivity, List<Movie> movies){
        this.mContext = context;
        this.mFatherActivity = fatherActivity;
        this.mMovies = movies;
        this.selectedItemPosition = -1;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title;
        public ImageView thumbnail;

        public ViewHolder(View itemView) {
            super(itemView);
            this.title = (TextView) itemView.findViewById(R.id.grid_item_movie_title);
            this.thumbnail = (ImageView) itemView.findViewById(R.id.grid_item_thumbnail);
            itemView.setOnClickListener(this);
            itemView.setTag(this); // to get the position of the selected element in the future
        }

        @Override
        public void onClick(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            int adapterPosition = holder.getAdapterPosition();
            Movie movie = mMovies.get(adapterPosition);


            Log.v(LOG_TAG, ">> selectedItemPosition: " + selectedItemPosition);
            Log.v(LOG_TAG, ">> adapterPosition: " + adapterPosition);


            Log.v(LOG_TAG, "(MoviesGridAdapter) movie.tmdbMovieId: " + movie.tmdbMovieId);
            Log.v(LOG_TAG, "(MoviesGridAdapter) movie.originalTitle: " + movie.originalTitle);
            Log.v(LOG_TAG, "(MoviesGridAdapter) movie.userRating: " + movie.userRating);


            // Highlight the selected item
            notifyItemChanged(selectedItemPosition);
            selectedItemPosition = adapterPosition;
            holder.itemView.setSelected(true);
            notifyItemChanged(selectedItemPosition);

            mFatherActivity.onMovieSelected(movie);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.grid_item, parent, false);
        itemView.setClickable(true);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Movie movie = mMovies.get(position);
        holder.title.setText(movie.title);

        Picasso.with(mContext)
                .load(movie.posterUrl)
                .placeholder(mContext.getResources().getDrawable(R.drawable.ic_image_placeholder))
                .error(android.R.drawable.stat_notify_error)
                .into(holder.thumbnail);

        holder.itemView.setSelected(selectedItemPosition == position);
    }

    @Override
    public int getItemCount() {
        return this.mMovies.size();
    }

    public void setSelectedItemPosition(int selectedItemPosition) {
        this.selectedItemPosition = selectedItemPosition;
    }

    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    public Movie getItem(int position) {
        return this.mMovies.get(position);
    }
}
