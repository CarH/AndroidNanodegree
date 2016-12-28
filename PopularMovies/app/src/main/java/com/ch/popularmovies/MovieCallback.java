package com.ch.popularmovies;

import com.ch.popularmovies.entities.Movie;

/**
 * Created by CarH on 12/12/2016.
 */

public interface MovieCallback {
    void onMovieSelected(Movie movie);
}
