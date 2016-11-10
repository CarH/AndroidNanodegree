package com.ch.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Map;
import java.util.Set;

import static com.ch.popularmovies.data.MovieContract.MovieEntry;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by CarH on 02/11/2016.
 */

public class TestUtility {

    static ContentValues createMovieValues() {
        ContentValues cv = new ContentValues();
        cv.put(MovieEntry.COLUMN_TITLE, "The Movie");
        cv.put(MovieEntry.COLUMN_RELEASE_DATE, "2016-10-21");
        cv.put(MovieEntry.COLUMN_SYNOPSIS, "The synopsis advanced.");
        cv.put(MovieEntry.COLUMN_USER_RATING, 6.5);
        return cv;
    }

    public static void validateCursorValues(Cursor cursor, ContentValues cv) {
        assertTrue("Empty cursor returned.", cursor.moveToFirst());
        validateValues(cursor, cv);
        cursor.close();
    }

    private static void validateValues(Cursor cursor, ContentValues contentValuesExpected) {
        Set<Map.Entry<String, Object>> valueSet = contentValuesExpected.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int pos = cursor.getColumnIndex(columnName);
            String actual =  cursor.getString(pos);
            String expected = entry.getValue().toString();
            assertEquals(String.format("Error: Expected %s , found: %s", expected, actual), expected, actual);
        }
    }
}
