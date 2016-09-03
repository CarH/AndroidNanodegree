package com.ch.popularmovies;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Avell G1743 MAX on 21/08/2016.
 */
public class DetailMovieFragment extends Fragment{
    private final String LOG_TAG = DetailMovieFragment.class.getSimpleName();
    private String mMovieTitle;
    private String mMovieSynopsis;
    private String mMovieReleaseDate;
    private String mMovieUserRating;
    private Bitmap mMoviePoster;

    public DetailMovieFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set all detail movie related data from the bundle
        Bundle movieInfo = getActivity().getIntent().getBundleExtra(getString(R.string.movie_bundle));

        this.mMovieTitle = movieInfo.getString(getString(R.string.movie_original_title));
        this.mMovieSynopsis = movieInfo.getString(getString(R.string.movie_synopsis));
        this.mMovieReleaseDate = movieInfo.getString(getString(R.string.movie_release_date));
        this.mMovieUserRating = movieInfo.getString(getString(R.string.movie_vote_average));

        String filename = movieInfo.getString(getString(R.string.movie_poster));
        this.mMoviePoster = getPosterImageFromFile(filename);
    }

    private Bitmap getPosterImageFromFile(String filename) {
        FileInputStream fis = null;
        try {

            fis = getActivity().openFileInput(filename);
            return BitmapFactory.decodeStream(fis);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
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

        moviePoster.setImageBitmap(this.mMoviePoster);
        movieTitle.setText(this.mMovieTitle);
        movieSynopsis.setText(this.mMovieSynopsis);
        movieReleaseDate.setText(this.mMovieReleaseDate);
        movieUserRating.setText(this.mMovieUserRating);


        movieSynopsis.setMovementMethod(new ScrollingMovementMethod());
        return root;
    }
}
