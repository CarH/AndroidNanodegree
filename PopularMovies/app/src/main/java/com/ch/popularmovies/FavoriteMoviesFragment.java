package com.ch.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ch.popularmovies.data.MovieContract.MovieEntry;

import static com.ch.popularmovies.utilities.Utility.isPortrait;


/**
 * A simple {@link Fragment} subclass.
 */
public class FavoriteMoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, Selectable {
    private static final String LOG_TAG = FavoriteMoviesFragment.class.getSimpleName();

    private static final String SAVED_SELECTED_POSITION_KEY = "selectedPosition";
    private static final int FAVORITE_LOADER_ID = 100;

    private static final String[] FAVORITE_MOVIES_PROJECTION = {
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_USER_RATING,
            MovieEntry.COLUMN_SYNOPSIS,
            MovieEntry.COLUMN_POSTER
    };
    final static int COL_FAV_MOVIE_ID = 0;
    final static int COL_FAV_MOVIE_TITLE = 1;
    final static int COL_FAV_MOVIE_RELEASE_DATE = 2;
    final static int COL_FAV_MOVIE_USER_RATING = 3;
    final static int COL_FAV_MOVIE_SYNOPSIS = 4;
    final static int COL_FAV_MOVIE_POSTER = 5;

    FavoriteMoviesAdapter mAdapter;
    private RecyclerView recyclerView;
    private MovieCallback mFatherActivity;
    private int selectedPosition;

    public FavoriteMoviesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        this.mFatherActivity = (MovieCallback) context;
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setting up selected position
        if (savedInstanceState == null || !savedInstanceState.containsKey(SAVED_SELECTED_POSITION_KEY))
            this.selectedPosition = -1;
        else
            this.selectedPosition = savedInstanceState.getInt(SAVED_SELECTED_POSITION_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.mAdapter = new FavoriteMoviesAdapter(getActivity(), this.mFatherActivity);
        this.mAdapter.setSelectedItemPosition(this.selectedPosition);

        View root = inflater.inflate(R.layout.fragment_favorite, container, false);

        this.recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view_favorite_movies);
        setUpRecyclerView(recyclerView);

        return root;
    }

    private void setUpRecyclerView(RecyclerView recyclerView) {
        int numCols = (isPortrait(getActivity())) ? 2 : 3;
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), numCols));
        recyclerView.setAdapter(this.mAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        int position = (this.mAdapter != null) ? this.mAdapter.getSelectedItemPosition() : -1;
        outState.putInt(SAVED_SELECTED_POSITION_KEY, position);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(FAVORITE_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MovieEntry.CONTENT_URI;
        String sortOrder = MovieEntry.COLUMN_CREATED_AT + " DESC";
        return new CursorLoader(getActivity(),
                uri,
                FAVORITE_MOVIES_PROJECTION,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "Number of Rows returned: " + String.valueOf(data.getCount()));
        this.mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        this.mAdapter.swapCursor(null);
    }

    @Override
    public void activateSelectedItem() {
        int position = (this.mAdapter != null) ? this.mAdapter.getSelectedItemPosition() : -1;
        if (position >= 0) {
            this.recyclerView.smoothScrollToPosition(position);
            this.mFatherActivity.onMovieSelected(this.mAdapter.getItem(position));
        }
    }
}
