package com.ch.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ch.popularmovies.entities.Movie;
import com.ch.popularmovies.utilities.Utility;

import java.io.FileNotFoundException;

/**
 * Created by CarH on 21/11/2016.
 */
public class FavoriteMoviesAdapter extends RecyclerView.Adapter<FavoriteMoviesAdapter.ViewHolder> {

    private final MovieCallback mFatherActivity;
    private Context mContext;
    private Cursor mCursor;
    private int selectedItemPosition;

    public FavoriteMoviesAdapter(Context context, MovieCallback fatherActivity) {
        this.mContext = context;
        this.mFatherActivity = fatherActivity;
        this.selectedItemPosition = -1;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        ImageView thumbnail;

        ViewHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.grid_item_movie_title);
            this.thumbnail = (ImageView) view.findViewById(R.id.grid_item_thumbnail);
            view.setOnClickListener(this);
            view.setTag(this);
        }

        @Override
        public void onClick(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            int adapterPosition = holder.getAdapterPosition();
            Movie movie = getMovieAt(adapterPosition);

            // Highlight the selected item
            notifyItemChanged(selectedItemPosition);
            selectedItemPosition = adapterPosition;
            holder.itemView.setSelected(true);
            notifyItemChanged(selectedItemPosition);

            mFatherActivity.onMovieSelected(movie);
        }
    }

    private Movie getMovieAt(int position) {
        Movie movie = new Movie();

        mCursor.moveToPosition(position);
        movie.tmdbMovieId = Long.parseLong(mCursor.getString(FavoriteMoviesFragment.COL_FAV_MOVIE_ID));
        movie.title = mCursor.getString(FavoriteMoviesFragment.COL_FAV_MOVIE_TITLE);
        movie.releaseDate = mCursor.getString(FavoriteMoviesFragment.COL_FAV_MOVIE_RELEASE_DATE);
        movie.synopsis = mCursor.getString(FavoriteMoviesFragment.COL_FAV_MOVIE_SYNOPSIS);
        movie.userRating = mCursor.getString(FavoriteMoviesFragment.COL_FAV_MOVIE_USER_RATING);
        movie.posterUrl = mCursor.getString(FavoriteMoviesFragment.COL_FAV_MOVIE_POSTER);

        return movie;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View movieView = LayoutInflater.from(mContext)
                .inflate(R.layout.grid_item, parent, false);
        movieView.setClickable(true);
        return new ViewHolder(movieView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        final String movieTitle = mCursor.getString(FavoriteMoviesFragment.COL_FAV_MOVIE_TITLE);
        final String posterPath = mCursor.getString(FavoriteMoviesFragment.COL_FAV_MOVIE_POSTER);
        Bitmap posterBitmap = null;
        try {
            posterBitmap = Utility.getBitmapFromFilename(posterPath);
        } catch (FileNotFoundException e) {
            // If the poster file was not found use a placeholder image
            posterBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_image_placeholder);
        }

        holder.title.setText(movieTitle);
        holder.thumbnail.setImageBitmap(posterBitmap);
        holder.itemView.setSelected(selectedItemPosition == position);
    }

    public void setSelectedItemPosition(int position) {
        this.selectedItemPosition = position;
    }

    public int getSelectedItemPosition() {
        return this.selectedItemPosition;
    }

    public Movie getItem(int position) {
        return getMovieAt(position);
    }



    @Override
    public int getItemCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    public void swapCursor(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return this.mCursor;
    }
}
