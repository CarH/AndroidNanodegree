package com.ch.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ch.popularmovies.data.MovieContract.MovieEntry;
import com.ch.popularmovies.entities.Movie;
import com.ch.popularmovies.utilities.Utility;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.ch.popularmovies.utilities.ConnectionUtility.isConnected;
import static com.ch.popularmovies.utilities.ConnectionUtility.showNotConnectedMessage;

/**
 * Created on 21/08/2016.
 */
public class DetailMovieFragment extends Fragment implements AppBarLayout.OnOffsetChangedListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = DetailMovieFragment.class.getSimpleName();
    private static final String DETAIL_MOVIE_KEY = "detail_movie";

    private static final int DETAIL_LOADER_ID = 100;

    private String mMovieTitle;
    private String mMovieSynopsis;
    private String mMovieReleaseDate;
    private String mMovieUserRating;
    private String mMoviePosterUrl;
    private String mTMDBMovieId;

    private ArrayList<Trailer> mTrailerList;    // Data Model
    private TrailerAdapter mTrailerAdapter;     // Adapter

    private ArrayList<Review> mReviewList;      // Data Model
    private ReviewAdapter mReviewAdapter;       // Adapter

    private FloatingActionButton mFab;
    private ImageView mMoviePoster;
    private View rootView;
    private TextView mPlaceholder;
    private boolean enablePlaceholder;
    private String mMovieOriginalTitle;

    // Number of stars in rating bar
    private final int NUMBER_OF_STARS = 5;
    private final int MAX_VOTE = 10;
    private final float FACTOR = (float) NUMBER_OF_STARS / (float) MAX_VOTE;


    public static DetailMovieFragment getInstance(Movie movie) {
        Bundle args = new Bundle();
        args.putParcelable(DETAIL_MOVIE_KEY, movie);
        DetailMovieFragment dmf = new DetailMovieFragment();
        dmf.setArguments(args);
        return dmf;
    }

    public DetailMovieFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Trailer data model settings and Adapter instantiation
        if (this.mTrailerList == null)
            this.mTrailerList = new ArrayList<Trailer>();
        this.mTrailerAdapter = new TrailerAdapter(this.mTrailerList);

        // Review data model settings and Adapter instantiation
        if (this.mReviewList == null)
            this.mReviewList = new ArrayList<Review>();
        this.mReviewAdapter = new ReviewAdapter(this.mReviewList);

        // Set all movie detail related data from the bundle AND set is favorite movie flag
        Bundle args = getArguments();
        this.enablePlaceholder = true;
        if (args != null) {
            Movie movie = args.getParcelable(DETAIL_MOVIE_KEY);
            if (movie != null) {
                this.mTMDBMovieId = String.valueOf(movie.tmdbMovieId);
                this.mMovieTitle = movie.title;
                this.mMovieOriginalTitle = movie.originalTitle;
                this.mMovieSynopsis = movie.synopsis;
                this.mMovieReleaseDate = movie.releaseDate;
                this.mMovieUserRating = movie.userRating;
                this.mMoviePosterUrl = movie.posterUrl;

                this.enablePlaceholder = false;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.detail_movie_fragment, container, false);

        this.rootView = root.findViewById(R.id.coordinator_layout_detail);

        final AppBarLayout appBarLayout = (AppBarLayout) root.findViewById(R.id.appbar);
        if (appBarLayout != null)
            appBarLayout.addOnOffsetChangedListener(this);

        this.mFab = (FloatingActionButton) root.findViewById(R.id.fab);
        setUpFabButton();

        final Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        if (toolbar != null)
            setDisplayHome(toolbar);

        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) root.findViewById(
                R.id.collapsing_toolbar_detail_movie);
        if (collapsingToolbar != null)
            collapsingToolbar.setTitle(this.mMovieTitle);

        this.mMoviePoster = (ImageView) root.findViewById(R.id.movie_poster);

        this.mPlaceholder = (TextView) root.findViewById(R.id.detail_placeholder);
        if (this.mPlaceholder != null) {
            this.setPlaceHolderVisibility(this.enablePlaceholder);
        }

        // Set the movie title on the Detail Activity in two pane mode
        final TextView detailTitle = (TextView) root.findViewById(R.id.detail_movie_title);
        if (detailTitle != null)
            detailTitle.setText(this.mMovieOriginalTitle);

        TextView movieReleaseDate = (TextView) root.findViewById(R.id.release_date);
        movieReleaseDate.setText(this.mMovieReleaseDate);

        RatingBar ratingBar = (RatingBar) root.findViewById(R.id.rating_bar);
        if (this.mMovieUserRating != null)
            ratingBar.setRating((float) Float.valueOf(this.mMovieUserRating) * FACTOR);

        TextView movieUserRating = (TextView) root.findViewById(R.id.user_rating);
        movieUserRating.setText(formatUserRating(this.mMovieUserRating));

        TextView movieSynopsis = (TextView) root.findViewById(R.id.synopsis);
        movieSynopsis.setText(this.mMovieSynopsis);

        RecyclerView trailerListView = (RecyclerView) root.findViewById(R.id.recycler_view_trailers);
        setUpRecyclerViewTrailers(trailerListView);
        populateTrailerList();

        RecyclerView reviewListView = (RecyclerView) root.findViewById(R.id.recycler_view_reviews);
        setUpRecyclerViewReviews(reviewListView);
        populateReviewList();

        return root;
    }

    /**
     * To control when the favorite button appears
     * @param appBarLayout
     * @param verticalOffset
     */
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (this.mFab == null)
            return;

        if (verticalOffset != 0)
            this.mFab.hide();
        else
            this.mFab.show();
    }

    private class FavoriteButtonOnClickListener implements View.OnClickListener {
        private String posterPath;

        @Override
        public void onClick(View view) {
            if (movieAlreadyIncludedInTheFavoriteList())
                removeFavoriteMovie();
            else
                persistFavoriteMovie();
        }

        private boolean movieAlreadyIncludedInTheFavoriteList() {
            Uri uri = MovieEntry.buildMovieUri(Long.valueOf(mTMDBMovieId));

            Cursor cursor = getActivity()
                    .getContentResolver()
                    .query(uri, null, null, null, null);

            boolean b = cursor.getCount() != 0;

            cursor.close();
            return b;
        }

        private void removeFavoriteMovie() {
            String selection = MovieEntry.TABLE_NAME + "." + MovieEntry._ID + " = ? ";
            showSnackBarMessage(getString(R.string.removing_movie_from_the_fav_list));
            getActivity()
                    .getContentResolver()
                    .delete(MovieEntry.CONTENT_URI,
                            selection,
                            new String[] {mTMDBMovieId});
            deletePoster();
            mFab.setImageResource(R.drawable.ic_favorite_border_white_24dp);
        }

        private void showSnackBarMessage(String msg) {
            Snackbar.make(rootView, msg, Snackbar.LENGTH_LONG)
                    .show();
        }

        private boolean deletePoster() {
            File file = new File(getFormattedFilename());
            return file.delete();
        }

        private void persistFavoriteMovie() {
            savePoster();

            ContentValues cv = new ContentValues();
            cv.put(MovieEntry._ID, mTMDBMovieId);
            cv.put(MovieEntry.COLUMN_TITLE, mMovieTitle);
            cv.put(MovieEntry.COLUMN_USER_RATING, mMovieUserRating);
            cv.put(MovieEntry.COLUMN_SYNOPSIS, mMovieSynopsis);
            cv.put(MovieEntry.COLUMN_RELEASE_DATE, mMovieReleaseDate);
            cv.put(MovieEntry.COLUMN_POSTER, posterPath);

            showSnackBarMessage(getString(R.string.inserting_movie_in_the_fav_list));
            getActivity().getContentResolver().insert(MovieEntry.CONTENT_URI, cv);
            mFab.setImageResource(R.drawable.ic_favorite_white_24dp);
        }

        private void savePoster() {
            if (mMoviePoster == null) {
                this.posterPath = null;
                return;
            }
            File filePath = createFilePath();
            Bitmap bitmap = ((BitmapDrawable)mMoviePoster.getDrawable()).getBitmap();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(filePath);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null)
                        fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.posterPath = filePath.getPath();
        }

        @NonNull
        private File createFilePath() {
            final String folderName = "posters";
            String filename = getFormattedFilename();
            ContextWrapper wrapper = new ContextWrapper(getActivity().getApplicationContext());
            File dirFile = wrapper.getDir(folderName, Context.MODE_PRIVATE);
            File pathFile = new File(dirFile, filename);

            return pathFile;
        }

        private String getFormattedFilename() {
            return String.format("%s_poster.jpg", mMovieTitle.replace(" ", "_"));
        }
    }

    private void setUpFabButton() {
        this.mFab.setOnClickListener(new FavoriteButtonOnClickListener());
    }

    private void setDisplayHome(Toolbar toolbar) {
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void fetchPoster() {
        try {
            Bitmap bm = Utility.getBitmapFromFilename(this.mMoviePosterUrl);
            this.mMoviePoster.setImageBitmap(bm);
        } catch (FileNotFoundException e) {
            Picasso.with(getActivity())
                    .load(this.mMoviePosterUrl)
                    .placeholder(getActivity().getResources().getDrawable(R.drawable.ic_image_placeholder))
                    .error(android.R.drawable.stat_notify_error)
                    .into(this.mMoviePoster);
        }
    }

    private String formatUserRating(String userRating) {
        return String.format("%s/10", userRating);
    }

    private void setUpRecyclerViewTrailers(RecyclerView trailerListView) {
        trailerListView.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        trailerListView.setAdapter(this.mTrailerAdapter);
    }

    private void populateTrailerList() {
        if (this.mTrailerList.isEmpty())
            fetchTrailerInfo();
    }

    private void fetchTrailerInfo() {
        if (!isConnected(getActivity()))
            showNotConnectedMessage(getActivity());
        else {
            StringRequest request = new StringRequest(StringRequest.Method.GET,
                    getTrailerListUrl(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray results = new JSONObject(response).getJSONArray("results");

                                for (int i = 0; i < results.length(); i++) {
                                    Trailer trailer = new Trailer();
                                    JSONObject trailerJson = results.getJSONObject(i);
                                    trailer.name = trailerJson.getString("name");
                                    trailer.url = composeTrailerUrl(trailerJson);

                                    mTrailerList.add(trailer);
                                }
                                mTrailerAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        private String composeTrailerUrl(JSONObject trailer) throws JSONException {
                            String SCHEME;
                            String AUTHORITY;
                            String BASE_PATH;
                            String QUERY_KEY;
                            Uri.Builder builder = new Uri.Builder();
                            if (trailer.getString("site").equals("YouTube")) {
                                String v  = trailer.getString("key");
                                SCHEME    = "https";
                                AUTHORITY = "www.youtube.com";
                                BASE_PATH = "watch";
                                QUERY_KEY = "v";
                                builder.scheme(SCHEME)
                                        .authority(AUTHORITY)
                                        .path(BASE_PATH)
                                        .appendQueryParameter(QUERY_KEY, v);
                            } else {
                                String name = trailer.getString("name");
                                SCHEME      = "https";
                                AUTHORITY   = "www.google.com.br";
                                BASE_PATH   = "search";
                                QUERY_KEY   = "q";
                                builder.scheme(SCHEME)
                                        .authority(AUTHORITY)
                                        .path(BASE_PATH)
                                        .appendQueryParameter(QUERY_KEY, name);
                            }
                            return builder.toString();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });
            RequestHandler.getInstance(getActivity()).addToRequestQueue(request);
        }
    }

    private String getTrailerListUrl() {
        String path = "videos";
        return composeUrlWithPath(path);
    }

    private String composeUrlWithPath(String path) {
        final String SCHEME     = "http";
        final String AUTHORITY  = "api.themoviedb.org";
        final String BASE_PATH  = "3/movie/";
        final String API_KEY    = "api_key";

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(AUTHORITY)
                .path(BASE_PATH)
                .appendPath(this.mTMDBMovieId)
                .appendPath(path)
                .appendQueryParameter(API_KEY, BuildConfig.THE_MOVIE_API_KEY);

        return builder.toString();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (this.mTMDBMovieId == null)
            return null;

        String[] projection = new String[] {MovieEntry._ID};
        Uri uri = MovieEntry.buildMovieUri(Long.valueOf(this.mTMDBMovieId));

        return new CursorLoader(getActivity(),
                uri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            setFavoriteMovie();
        }

        // Now that we know if the movie is a favorite one, we can load the poster properly
        fetchPoster();
    }

    private void setFavoriteMovie() {
        // set the fav icon to be a filled heart
        this.mFab.setImageResource(R.drawable.ic_favorite_white_24dp);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void setPlaceHolderVisibility(boolean placeHolderVisibility) {
        if (placeHolderVisibility)
            this.mPlaceholder.setVisibility(View.VISIBLE);
        else
            this.mPlaceholder.setVisibility(View.INVISIBLE);
    }


    private class Trailer {
        public String name;
        public String url;
    }


    class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.ViewHolder> {
        private List<Trailer> mTrailers;

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private TextView mTrailerTitleView;

            ViewHolder(View itemView) {
                super(itemView);
                mTrailerTitleView = (TextView) itemView.findViewById(R.id.textview_trailer_title);
                itemView.setTag(this);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                ViewHolder holder = (ViewHolder) view.getTag();
                Trailer trailer = mTrailers.get(holder.getAdapterPosition());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailer.url));
                if (intent.resolveActivity(getActivity().getPackageManager()) != null)
                    startActivity(intent);
            }
        }

        TrailerAdapter(List<Trailer> trailers) { this.mTrailers = trailers; }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(getContext())
                    .inflate(R.layout.trailer_list_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mTrailerTitleView.setText(this.mTrailers.get(position).name);
        }

        @Override
        public int getItemCount() {
            return this.mTrailers.size();
        }
    }


    private void setUpRecyclerViewReviews(RecyclerView reviewListView) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity()){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        reviewListView.setLayoutManager(linearLayoutManager);
        reviewListView.setAdapter(this.mReviewAdapter);
    }

    private void populateReviewList() {
        if (this.mReviewList.isEmpty())
            fetchReviews();
    }

    private void fetchReviews() {
        if (!isConnected(getActivity()))
            showNotConnectedMessage(getActivity());
        else {
            StringRequest request = new StringRequest(StringRequest.Method.GET,
                    getReviewsUrl(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray reviews = (new JSONObject(response)).getJSONArray("results");
                                JSONObject reviewJSON = null;
                                for (int i = 0; i < reviews.length(); i++) {
                                    reviewJSON = reviews.getJSONObject(i);
                                    Review review = new Review();
                                    review.author = reviewJSON.getString("author");
                                    review.content = reviewJSON.getString("content");
                                    mReviewList.add(review);
                                }
                                mReviewAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.v(LOG_TAG, "FETCHING REVIEWS ERROR: " + error.getMessage());
                        }
                    });
            RequestHandler.getInstance(getActivity()).addToRequestQueue(request);
        }
    }

    private String getReviewsUrl() {
        return composeUrlWithPath("reviews");
    }

    private class Review {
        public String author;
        public String content;
    }

    class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
        private List<Review> mReviews;

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView author;
            TextView content;

            public ViewHolder(View itemView) {
                super(itemView);
                author = (TextView) itemView.findViewById(R.id.review_author);
                content = (TextView) itemView.findViewById(R.id.review_content);
            }
        }

        public ReviewAdapter(List<Review> reviews) { this.mReviews = reviews; }

        @Override
        public ReviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.review_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ReviewAdapter.ViewHolder holder, int position) {
            Review review = this.mReviews.get(position);
            holder.content.setText(review.content);
            holder.author.setText(review.author);
        }

        @Override
        public int getItemCount() {
            return this.mReviews.size();
        }
    }
}
