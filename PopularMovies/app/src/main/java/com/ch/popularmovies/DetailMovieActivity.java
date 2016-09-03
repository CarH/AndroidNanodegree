package com.ch.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class DetailMovieActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_movie);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.detail_container, new DetailMovieFragment())
                    .commit();
        }
    }

}
