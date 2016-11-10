package com.ch.popularmovies;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
 * Created on 21/08/2016.
 */
public class DetailMovieFragment extends Fragment implements AdapterView.OnItemClickListener {
    private final String LOG_TAG = DetailMovieFragment.class.getSimpleName();
    private String mMovieTitle;
    private String mMovieSynopsis;
    private String mMovieReleaseDate;
    private String mMovieUserRating;
    private String mMoviePosterUrl;
    private String mMovieId;
    private List<Trailer> mTrailerList;     // Data Model
    private ArrayAdapter mTrailerAdapter;   // Adapter


    public DetailMovieFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.mTrailerList == null)
            this.mTrailerList = new ArrayList<Trailer>();

        // Set all movie detail related data from the bundle
        Bundle movieInfo = getActivity().getIntent().getBundleExtra(getString(R.string.movie_bundle));

        this.mMovieId = movieInfo.getString(getString(R.string.movie_id));
        this.mMovieTitle = movieInfo.getString(getString(R.string.movie_original_title));
        this.mMovieSynopsis = movieInfo.getString(getString(R.string.movie_synopsis));
        this.mMovieReleaseDate = movieInfo.getString(getString(R.string.movie_release_date));
        this.mMovieUserRating = movieInfo.getString(getString(R.string.movie_vote_average));
        this.mMoviePosterUrl = movieInfo.getString(getString(R.string.movie_poster));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.detail_movie_fragment, container, false);

        ImageView moviePoster = (ImageView) root.findViewById(R.id.movie_poster);
        TextView movieTitle = (TextView) root.findViewById(R.id.original_title);
        TextView movieSynopsis = (TextView) root.findViewById(R.id.synopsis);
        TextView movieReleaseDate = (TextView) root.findViewById(R.id.release_date);
        TextView movieUserRating = (TextView) root.findViewById(R.id.user_rating);
        ListView trailerListView = (ListView) root.findViewById(R.id.trailer_list_view);

        Picasso.with(getActivity()).load(mMoviePosterUrl).into(moviePoster);
        movieTitle.setText(this.mMovieTitle);
        movieSynopsis.setText(this.mMovieSynopsis);
        movieReleaseDate.setText(this.mMovieReleaseDate);
        movieUserRating.setText(this.mMovieUserRating);

//        movieSynopsis.setMovementMethod(new ScrollingMovementMethod());
        this.mTrailerAdapter = new TrailerAdapter(getActivity(), R.layout.trailer_list_item, this.mTrailerList);
        trailerListView.setAdapter(this.mTrailerAdapter);

        if (this.mTrailerList.isEmpty())
            fetchTrailerInfo();
        trailerListView.setOnItemClickListener(this);
        return root;
    }

    private void fetchTrailerInfo() {
        if (!isConnected(getActivity()))
            printNotConnectedMessage(getActivity());
        else {
            StringRequest request = new StringRequest(StringRequest.Method.GET,
                    getTrailerListUrl(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Log.v(LOG_TAG, "response: "+response);
                                JSONArray results = new JSONObject(response).getJSONArray("results");

                                for (int i = 0; i < results.length(); i++) {
                                    Trailer trailer = new Trailer();
                                    JSONObject trailerJson = results.getJSONObject(i);
                                    trailer.name = trailerJson.getString("name");
                                    trailer.url = composeTrailerUrl(trailerJson);
                                    Log.v(LOG_TAG, " Trailer Name: " + trailer.name + ", url: "+ trailer.url);

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
        final String SCHEME     = "http";
        final String AUTHORITY  = "api.themoviedb.org";
        final String BASE_PATH  = "3/movie/";
        final String EXT_PATH   = "videos";
        final String API_KEY    = "api_key";

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(AUTHORITY)
                .path(BASE_PATH)
                .appendPath(this.mMovieId)
                .appendPath(EXT_PATH)
                .appendQueryParameter(API_KEY, BuildConfig.THE_MOVIE_API_KEY);

        return builder.toString();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Trailer trailer = (Trailer) adapterView.getItemAtPosition(i);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailer.url));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null)
            startActivity(intent);
    }

    private class Trailer {
        public String name;
        public String url;
    }


    class TrailerAdapter extends ArrayAdapter<Trailer> {

        public TrailerAdapter(Context context, int resource, List<Trailer> trailers) {
            super(context, resource, trailers);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.trailer_list_item, parent, false);
            }
            Trailer trailer = (Trailer) mTrailerAdapter.getItem(position);
            TextView tv = (TextView) convertView.findViewById(R.id.textview_trailer);
            tv.setText(trailer.name);
            return convertView;
        }
    }
}
