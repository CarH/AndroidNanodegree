package com.ch.popularmovies;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ch.popularmovies.entities.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.ch.popularmovies.utilities.ConnectionUtility.isConnected;
import static com.ch.popularmovies.utilities.ConnectionUtility.showNotConnectedMessage;
import static com.ch.popularmovies.utilities.Utility.isPortrait;


/**
 * A simple {@link Fragment} subclass.
 */
public class MoviesFragment extends Fragment implements Selectable {
    private static final String LOG_TAG = MoviesFragment.class.getSimpleName();
    public static final String ORDER_BY_KEY = "order_by";
    private static final String SAVED_MOVIES_KEY =  "movies";
    private static final String SAVED_SELECTED_POSITION_KEY = "selectedPosition";

    private ArrayList<Movie> mMovies;       // Data Model
    private MoviesGridAdapter mAdapter;     // Adapter
    private MovieCallback mFatherActivity;
    private int selectedPosition;
    private RecyclerView recyclerView;

    public MoviesFragment() {}

    @Override
    public void onAttach(Context context) {
        this.mFatherActivity = (MovieCallback) context;
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setting up the movie list
        if (savedInstanceState == null || !savedInstanceState.containsKey(SAVED_MOVIES_KEY))
            this.mMovies = new ArrayList<>();
        else
            this.mMovies = savedInstanceState.getParcelableArrayList(SAVED_MOVIES_KEY);

        // Setting up selected position
        if (savedInstanceState == null || !savedInstanceState.containsKey(SAVED_SELECTED_POSITION_KEY))
            this.selectedPosition = -1;
        else
            this.selectedPosition = savedInstanceState.getInt(SAVED_SELECTED_POSITION_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.mAdapter = new MoviesGridAdapter(getContext(), mFatherActivity, mMovies);
        this.mAdapter.setSelectedItemPosition(this.selectedPosition);

        this.recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_movies, container, false);
        setUpRecyclerView();

        fetchMoviesToPopulateTheGrid();

        return recyclerView;
    }

    private void setUpRecyclerView() {
        int numCols = (isPortrait(getActivity())) ? 2 : 3;
        this.recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), numCols));
        this.recyclerView.setAdapter(this.mAdapter);
        scrollToSelectedItemPosition();
    }

    private void scrollToSelectedItemPosition() {
        int selectedItemPosition = this.mAdapter.getSelectedItemPosition();
        if (selectedItemPosition >= 0)
            this.recyclerView.smoothScrollToPosition(selectedItemPosition);
    }

    private void fetchMoviesToPopulateTheGrid() {
        if (!this.mMovies.isEmpty())
            return;
        if (!isConnected(getContext()))
            showNotConnectedMessage(getContext());
        else {
            // Using Volley as suggested by a reviewer
            StringRequest stringRequest = new StringRequest(Request.Method.GET,
                    getMovieListUrl(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response == null) return;
                            parseResponseAndUpdateMovieList(response);
                        }

                        private void parseResponseAndUpdateMovieList(String response) {
                            try {
                                JSONArray jsonArray = new JSONObject(response).getJSONArray("results");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonMovie = jsonArray.getJSONObject(i);
                                    Movie movie = new Movie();

                                    movie.tmdbMovieId = jsonMovie.getLong("id");
                                    movie.title = jsonMovie.getString("title");
                                    movie.originalTitle = jsonMovie.getString("original_title");
                                    movie.synopsis = jsonMovie.getString("overview");
                                    movie.popularity = jsonMovie.getString("popularity");
                                    movie.voteCount = jsonMovie.getString("vote_count");
                                    movie.userRating = jsonMovie.getString("vote_average");
                                    movie.posterUrl = getPosterUrl(jsonMovie.getString("poster_path"));
                                    movie.releaseDate = getFormattedDate(jsonMovie.getString("release_date"));

                                    mMovies.add(movie);
                                }
                                mAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Nullable
                        private String getPosterUrl(String posterPath) {
                            return (posterPath.equals("null")) ? null
                                    : composePosterUrl(posterPath);
                        }

                        private String getFormattedDate(String releaseDate) {
                            String[] splitDate = releaseDate.split("-");
                            return (splitDate.length == 3) ?
                                    splitDate[1] + "/" + splitDate[2] + "/" + splitDate[0] :
                                    releaseDate;
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.v(LOG_TAG, "FETCHING MOVIES ERROR: " + error.getMessage());
                        }
                    });

            RequestHandler.getInstance(getContext()).addToRequestQueue(stringRequest);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        int position = (this.mAdapter != null) ? this.mAdapter.getSelectedItemPosition() : -1;

        outState.putParcelableArrayList(SAVED_MOVIES_KEY, this.mMovies);
        outState.putInt(SAVED_SELECTED_POSITION_KEY, position);
        super.onSaveInstanceState(outState);
    }

    private String getMovieListUrl() {
        final String SCHEME     = "http";
        final String AUTHORITY  = "api.themoviedb.org";
        final String BASE_PATH  = "3/movie/";
        final String API_KEY    = "api_key";

        String prefOrder = getOrderByCriteria();

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(AUTHORITY)
                .path(BASE_PATH)
                .appendPath(prefOrder)
                .appendQueryParameter(API_KEY, BuildConfig.THE_MOVIE_API_KEY);

        return builder.toString();
    }

    private String getOrderByCriteria() {
        return getArguments().getString(ORDER_BY_KEY);
    }

    @NonNull
    private String composePosterUrl(String path) {
        final String SCHEME     = "https";
        final String AUTHORITY  = "image.tmdb.org";
        final String PATH       = "t/p/w500";

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(AUTHORITY)
                .appendEncodedPath(PATH)
                .appendEncodedPath(path);

        return builder.toString();
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