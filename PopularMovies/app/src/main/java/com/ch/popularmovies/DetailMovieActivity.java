package com.ch.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ch.popularmovies.entities.Movie;

public class DetailMovieActivity extends AppCompatActivity {
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_movie);
        if (savedInstanceState == null) {
            Movie movie = getIntent().getParcelableExtra("movie");
            DetailMovieFragment dmf = DetailMovieFragment.getInstance(movie);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_container, dmf, DETAILFRAGMENT_TAG)
                    .commit();
        }
    }
}
