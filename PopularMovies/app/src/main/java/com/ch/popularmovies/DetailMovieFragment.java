package com.ch.popularmovies;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created on 21/08/2016.
 */
public class DetailMovieFragment extends Fragment{
    private final String LOG_TAG = DetailMovieFragment.class.getSimpleName();
    private String mMovieTitle;
    private String mMovieSynopsis;
    private String mMovieReleaseDate;
    private String mMovieUserRating;
    private String mMoviePosterUrl;

    public DetailMovieFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set all movie detail related data from the bundle
        Bundle movieInfo = getActivity().getIntent().getBundleExtra(getString(R.string.movie_bundle));

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

        Picasso.with(getActivity()).load(mMoviePosterUrl).into(moviePoster);
        movieTitle.setText(this.mMovieTitle);
        movieSynopsis.setText(this.mMovieSynopsis);
        movieReleaseDate.setText(this.mMovieReleaseDate);
        movieUserRating.setText(this.mMovieUserRating);


        movieSynopsis.setMovementMethod(new ScrollingMovementMethod());
        return root;
    }
}
