package com.ch.popularmovies;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


/**
 * A simple {@link Fragment} subclass.
 */
public class MoviesFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String LOG_TAG = MoviesFragment.class.getSimpleName();

    // I've created the list below in order to save the movie objects that I've downloaded via API.
    // Is that the ideal way of doing it?
    private ArrayList<Movie> mMovies;

    private ArrayAdapter mAdapter;

    private String mPrefOrder;

    public MoviesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            this.mMovies = new ArrayList<>();
        } else {
            this.mMovies = savedInstanceState.getParcelableArrayList("movies");
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        this.mPrefOrder = sharedPrefs.getString(getString(R.string.pref_order_by_key), "");
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
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        // To update the movie grid IF the order_by value has been changed
        // I know it seems strange... suggestions are welcome!
        String pref_order = sharedPrefs.getString(getString(R.string.pref_order_by_key), "");
        if (!pref_order.equals(this.mPrefOrder)){
            this.mPrefOrder = pref_order;
            this.mAdapter.clear();
            this.mMovies.clear();
            fetchMoviesToPopulateTheGrid();
        }
    }

    private void fetchMoviesToPopulateTheGrid() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String pref_order = sharedPrefs.getString(getString(R.string.pref_order_by_key), "");

        if (isConnected()) {
            (new FetchMoviesTask()).execute(pref_order);
        }
        else {
            printNotConnectedMessage();
        }
    }

    private void printNotConnectedMessage() {
        Toast.makeText(getContext(), "No internet connection :(", Toast.LENGTH_LONG).show();
    }

    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int i, long l) {

        Movie movie = (Movie) adapter.getItemAtPosition(i);

        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.movie_original_title), movie.originalTitle);
        bundle.putString(getString(R.string.movie_synopsis), movie.overview);
        bundle.putString(getString(R.string.movie_vote_average), movie.voteAverage);
        bundle.putString(getString(R.string.movie_release_date), movie.releaseDate);

        // I know that this is too much code to just pass an image to an activity
        // unfortunately it was what I found :( . Any suggestion would be appreciated :D
        String filename = "poster.png";
        ImageView posterImageView = (ImageView) view.findViewById(R.id.grid_item_thumbnail);
        saveImageViewInFile(posterImageView, filename);
        bundle.putString(getString(R.string.movie_poster), filename);

        Intent intent = new Intent(getContext(), DetailMovieActivity.class);
        intent.putExtra(getString(R.string.movie_bundle), bundle);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void saveImageViewInFile(ImageView posterImageView, String filename) {
        FileOutputStream stream = null;
        Bitmap imageBitmap = null;
        try {
            stream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);

            imageBitmap = ((BitmapDrawable) posterImageView.getDrawable()).getBitmap();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void fetchPosterImage(int position, ViewHolder holder, String posterPath) {
        if (isConnected()) {
            (new FetchPosterImageTask(position, holder))
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, posterPath);
        }
        else {
            printNotConnectedMessage();
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
        public String posterPath;
        public Bitmap posterImage;

        public Movie(){
            posterImage = null;
        }

        protected Movie(Parcel in) {
            title = in.readString();
            originalTitle = in.readString();
            overview = in.readString();
            releaseDate = in.readString();
            popularity = in.readString();
            voteCount = in.readString();
            voteAverage = in.readString();
            posterPath = in.readString();
            posterImage = in.readParcelable(Bitmap.class.getClassLoader());
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
            parcel.writeString(posterPath);
            parcel.writeParcelable(posterImage, i);
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
            if (!movie.posterPath.equals("null")) {
                Bitmap imageBitmap = mMovies.get(position).posterImage;
                // Check if it has already been downloaded. If yes, use it instead of query again
                if (imageBitmap != null) {
                    holder.thumbnail.setImageBitmap(imageBitmap);
                } else {
                    // Place a placeholder image on the current view to be displayed until the
                    // correspondent poster image arrive
                    fetchPosterImage(position, holder, movie.posterPath);
                }
            }
            else { // otherwise sets the placeholder image as the poster image
                holder.thumbnail.setImageResource(R.drawable.ic_image_not_found);
                holder.thumbnail.setScaleType(ImageView.ScaleType.FIT_XY);
            }

            return convertView;
        }
    }


    class FetchPosterImageTask extends AsyncTask <String, Void, Bitmap> {

        private int mPosition;
        private ViewHolder mViewHolder;

        private final String SCHEME     = "https";
        private final String AUTHORITY  = "image.tmdb.org";
        private final String PATH       = "t/p/w500";

        public FetchPosterImageTask(int position, ViewHolder viewHolder) {
            this.mPosition = position;
            this.mViewHolder = viewHolder;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            if (params.length != 1 || params[0].equals(null))
                return null;

            String posterPath = params[0];

            // To allow closing the connection socket and the input stream in the finally block
            HttpsURLConnection urlConnection = null;
            InputStream inputStream = null;

            Bitmap result = null;

            Uri.Builder builder = new Uri.Builder();
            builder.scheme(SCHEME)
                    .authority(AUTHORITY)
                    .appendEncodedPath(PATH)
                    .appendEncodedPath(posterPath);

            try {
                URL url = new URL(builder.toString());

                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.connect();

                inputStream = urlConnection.getInputStream();

                if (inputStream != null) {
                    result = BitmapFactory.decodeStream(inputStream);
                }
                else {
                    result = null;
                }

                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        public void onPostExecute(Bitmap posterImage) {
            // If the view has not been recycled yet, set the poster
            if (this.mPosition == this.mViewHolder.position){
                // If there isn't a poster image, put a placeholder
                if (posterImage == null) {
                    this.mViewHolder.thumbnail.setImageResource(R.drawable.ic_image_not_found);
                }
                else{
                    this.mViewHolder.thumbnail.setImageBitmap(posterImage);
                    mMovies.get(this.mPosition).posterImage = posterImage;
                }
            }
        }
    }


    class FetchMoviesTask extends AsyncTask<String, String, String> {
        private final String SCHEME     = "https";
        private final String AUTHORITY  = "api.themoviedb.org";
        private final String PATH       = "3/movie";


        @Override
        protected String doInBackground(String... params) {
            final String SORT_BY        = "sort_by";
            final String API_KEY        = "api_key";

            if (params.length != 1) {
                return null;
            }

            String sortByValue = params[0];
            String jsonRes = null;
            HttpsURLConnection conn;
            Uri.Builder builder = new Uri.Builder();

            builder.scheme(SCHEME)
                    .authority(AUTHORITY)
                    .path(PATH)
                    .appendQueryParameter(SORT_BY, sortByValue)
                    .appendQueryParameter(API_KEY, BuildConfig.THE_MOVIE_API_KEY);

            try {

                URL url = new URL(builder.toString());
                conn = (HttpsURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.connect();

                InputStream inputStream = conn.getInputStream();
                BufferedReader reader   = new BufferedReader(new InputStreamReader(inputStream));

                if (inputStream == null) {
                    jsonRes = null;
                }

                String line = "";
                StringBuffer buffer = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                }

                if (buffer.length() > 0) {
                    jsonRes = buffer.toString();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return jsonRes;
        }

        @Override
        protected void onPostExecute(String jsonStr) {
            if (jsonStr == null) return;
            try {
                JSONArray jsonArray = new JSONObject(jsonStr).getJSONArray("results");
                for (int i=0; i < jsonArray.length(); i++) {
                    Movie movie = new Movie();
                    movie.title 		= jsonArray.getJSONObject(i).getString("title");
                    movie.originalTitle = jsonArray.getJSONObject(i).getString("original_title");
                    movie.overview 		= jsonArray.getJSONObject(i).getString("overview");
                    movie.popularity 	= jsonArray.getJSONObject(i).getString("popularity");
                    movie.voteCount 	= jsonArray.getJSONObject(i).getString("vote_count");
                    movie.voteAverage 	= jsonArray.getJSONObject(i).getString("vote_average");
                    movie.posterPath 	= jsonArray.getJSONObject(i).getString("poster_path");

                    String releaseDate  = jsonArray.getJSONObject(i).getString("release_date");
                    String[] splittedDate = releaseDate.split("-");
                    if (splittedDate.length == 3) {
                        movie.releaseDate = splittedDate[1] + "/" + splittedDate[2] + "/" + splittedDate[0];
                    }
                    else {
                        movie.releaseDate = releaseDate;
                    }

                    mMovies.add(movie);
                }
                mAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
