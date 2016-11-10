package com.ch.popularmovies;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.ch.popularmovies.utilities.ConnectionUtility.isConnected;
import static com.ch.popularmovies.utilities.ConnectionUtility.printNotConnectedMessage;


/**
 * A simple {@link Fragment} subclass.
 */
public class MoviesFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String LOG_TAG = MoviesFragment.class.getSimpleName();
    public static final String ORDER_BY_KEY = "order_by";

    // I've created the list below in order to save the movie objects that I've downloaded via API.
    // Is that the ideal way of doing it?
    private ArrayList<Movie> mMovies;

    private ArrayAdapter mAdapter;

    public MoviesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Setting up the movie list
        if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            this.mMovies = new ArrayList<>();
        } else {
            this.mMovies = savedInstanceState.getParcelableArrayList("movies");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.settings:
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_movies, container, false);

        this.mAdapter = new MoviesGridAdapter(getContext(), R.layout.grid_item, this.mMovies);

        GridView gridView = (GridView) root.findViewById(R.id.grid_movies);
        gridView.setAdapter(this.mAdapter);

        gridView.setOnItemClickListener(this);

        if (this.mMovies.isEmpty()) {
            fetchMoviesToPopulateTheGrid();
        }

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies", this.mMovies);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void fetchMoviesToPopulateTheGrid() {
        if (!isConnected(getContext()))
            printNotConnectedMessage(getContext());
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

                                    movie.id = jsonMovie.getLong("id");
                                    movie.title = jsonMovie.getString("title");
                                    movie.originalTitle = jsonMovie.getString("original_title");
                                    movie.overview = jsonMovie.getString("overview");
                                    movie.popularity = jsonMovie.getString("popularity");
                                    movie.voteCount = jsonMovie.getString("vote_count");
                                    movie.voteAverage = jsonMovie.getString("vote_average");
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
                            Log.v(LOG_TAG, "ERROR (Volley): " + error.getMessage());
                        }
                    });

            RequestHandler.getInstance(getContext()).addToRequestQueue(stringRequest);
        }
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

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int i, long l) {

        Movie movie = (Movie) adapter.getItemAtPosition(i);

        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.movie_id), String.valueOf(movie.id));
        bundle.putString(getString(R.string.movie_original_title), movie.originalTitle);
        bundle.putString(getString(R.string.movie_synopsis), movie.overview);
        bundle.putString(getString(R.string.movie_vote_average), movie.voteAverage);
        bundle.putString(getString(R.string.movie_release_date), movie.releaseDate);
        bundle.putString(getString(R.string.movie_poster), movie.posterUrl);

        Intent intent = new Intent(getContext(), DetailMovieActivity.class);
        intent.putExtra(getString(R.string.movie_bundle), bundle);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    class Movie implements Parcelable{
        public String title;
        public String originalTitle;
        public String overview;
        public String releaseDate;
        public String popularity;
        public String voteCount;
        public String voteAverage;
        public String posterUrl;
        public long id;

        public Movie(){
        }

        protected Movie(Parcel in) {
            title = in.readString();
            originalTitle = in.readString();
            overview = in.readString();
            releaseDate = in.readString();
            popularity = in.readString();
            voteCount = in.readString();
            voteAverage = in.readString();
            posterUrl = in.readString();
        }

        public final Creator<Movie> CREATOR = new Creator<Movie>() {
            @Override
            public Movie createFromParcel(Parcel in) {
                return new Movie(in);
            }

            @Override
            public Movie[] newArray(int size) {
                return new Movie[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(title);
            parcel.writeString(originalTitle);
            parcel.writeString(overview);
            parcel.writeString(releaseDate);
            parcel.writeString(popularity);
            parcel.writeString(voteCount);
            parcel.writeString(voteAverage);
            parcel.writeString(posterUrl);
        }
    }

    class ViewHolder {
        public TextView title;
        public ImageView thumbnail;
        public int position;
    }

    class MoviesGridAdapter extends ArrayAdapter<Movie> {

        public MoviesGridAdapter(Context context, int resource, List objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.grid_item, parent, false);

                ViewHolder holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.grid_item_movie_title);
                holder.thumbnail = (ImageView) convertView.findViewById(R.id.grid_item_thumbnail);
                convertView.setTag(holder);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            Movie movie = (Movie) mAdapter.getItem(position);

            holder.title.setText(movie.title);
            holder.position = position;

            // Checks if a valid poster path has been given
            if (movie.posterUrl != null) {
                // TODO :: REMOVE
                // Picasso.with(getContext()).setIndicatorsEnabled(true);

                Picasso.with(getContext())
                        .load(movie.posterUrl)
                        .placeholder(getContext().getResources().getDrawable(R.drawable.ic_image_placeholder))
                        .error(android.R.drawable.stat_notify_error)
                        .into(holder.thumbnail);
            }
            else { // otherwise sets the placeholder image as the poster image
                holder.thumbnail.setImageResource(R.drawable.ic_image_placeholder);
                holder.thumbnail.setScaleType(ImageView.ScaleType.FIT_XY);
            }

            return convertView;
        }
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
}