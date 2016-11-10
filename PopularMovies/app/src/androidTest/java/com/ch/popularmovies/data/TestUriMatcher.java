package com.ch.popularmovies.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by CarH on 16/10/2016.
 */

public class TestUriMatcher extends AndroidTestCase {
    public static long MOVIE_ID = 1L;
    public static final Uri TEST_MOVIE_DIR = MovieContract.MovieEntry.CONTENT_URI;
    public static final Uri TEST_MOVIE_ITEM = MovieContract.MovieEntry.buildMovieUri(MOVIE_ID);

    public void testUriMatcher() {
        UriMatcher testMatcher = MovieProvider.buildMatcher();

        assertEquals("Error: The MOVIE URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_DIR), MovieProvider.MOVIE);
        assertEquals("Error: The MOVIE WITH ID URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_ITEM), MovieProvider.MOVIE_WITH_ID);
    }
}
