package com.ch.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

/**
 * Created by CarH on 16/10/2016.
 */

public class TestProvider extends AndroidTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    private void deleteAllRecords() {
        // TODO:: replace this to deleteAllRecordsFromProvider as soon as you implement it
//        deleteAllRecordsFromDB();
        deleteAllRecordsFromProvider();
    }

    public void deleteAllRecordsFromDB() {
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(MovieContract.MovieEntry.TABLE_NAME, null, null);
        db.close();
    }

    public void deleteAllRecordsFromProvider() {
        int nrows = mContext.getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI, null, null);
        Cursor cursor = mContext.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        assertEquals("Error: Could not delete all registers from movie table.", 0, cursor.getCount());
        cursor.close();
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MovieProvider.class.getName());
        try {
            // Fetch the provider info using the anager
            // This throws an component name from the PackageMexception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MovieProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + MovieContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MovieContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MovieProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    public void testGetType() {
        // 1- content://com.ch.popularmovies/movie
        String type = getContext().getContentResolver().getType(MovieContract.MovieEntry.CONTENT_URI);
        assertEquals("Error: the URI type returned is wrong. It should have returned MovieEntry.CONTENT_TYPE.",
                MovieContract.MovieEntry.CONTENT_TYPE, type);
        // 2- content://com.ch.popularmovies/movie/#
        type = getContext().getContentResolver().getType(MovieContract.MovieEntry.buildMovieUri(10000));
        assertEquals("Error: the URI type returned is wrong. It should have returned MovieEntry.CONTENT_TYPE_ITEM.",
                MovieContract.MovieEntry.CONTENT_TYPE_ITEM, type);
    }

    public void testSimpleQuery() {
        SQLiteDatabase db = (new MovieDbHelper(mContext)).getWritableDatabase();
        ContentValues cv = TestUtility.createMovieValues();

        long rowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, cv);
        assertTrue("Error: Unable to insert the data into the table.", rowId != -1);

        db.close();

        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        TestUtility.validateCursorValues(cursor, cv);
    }

    public void testQueryWithMovieId() {
        SQLiteDatabase db = (new MovieDbHelper(mContext)).getWritableDatabase();
        ContentValues cv = TestUtility.createMovieValues();

        long rowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, cv);
        assertTrue("Error: Unable to insert the data into the table.", rowId != -1);

        db.close();

        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.buildMovieUri(rowId),
                null,
                null,
                null,
                null);

        TestUtility.validateCursorValues(cursor, cv);
    }

    public void testSimpleInsert() {
        ContentValues cv = TestUtility.createMovieValues();
        mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, cv);

        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        TestUtility.validateCursorValues(cursor, cv);
    }
}




