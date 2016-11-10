package com.ch.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.ch.popularmovies.data.MovieContract.MovieEntry;

/**
 * Created by CarH on 16/10/2016.
 */

public class MovieProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildMatcher();
    static final int MOVIE = 100;
    static final int MOVIE_WITH_ID = 101;

    private MovieDbHelper mOpenHelper;
    private static final String sMovieIdSelection = MovieEntry.TABLE_NAME +
            "." + MovieEntry._ID + " = ?";

    public static UriMatcher buildMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE, MOVIE);
        matcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_ID);
        return matcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        int t = sUriMatcher.match(uri);

        Cursor cursor = null;
        switch (t){
            // movie
            case MOVIE: {
                cursor = getMovieList(projection, selection, selectionArgs, sortOrder);
                break;
            }
            // movie/#
            case MOVIE_WITH_ID: {
                cursor = getMovieWithId(uri, projection);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private Cursor getMovieList(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(MovieEntry.TABLE_NAME);
        return queryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getMovieWithId(Uri uri, String[] projection) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(MovieEntry.TABLE_NAME);

        String id = MovieEntry.getMovieIdFromUri(uri);
        return queryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sMovieIdSelection,
                new String[]{id},
                null,
                null,
                null);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        int t = sUriMatcher.match(uri);
        switch (t){
            case MOVIE:
                return MovieEntry.CONTENT_TYPE;
            case MOVIE_WITH_ID:
                return MovieEntry.CONTENT_TYPE_ITEM;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri retUri = null;

        int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE:
            {
                long rowId = db.insert(MovieEntry.TABLE_NAME, null, contentValues);
                if (rowId > 0)
                    retUri = MovieContract.MovieEntry.buildMovieUri(rowId);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        int numRowsDeleted=0;
        switch (match){
            case MOVIE:
            {
                numRowsDeleted = db.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numRowsDeleted > 0)
           getContext().getContentResolver().notifyChange(uri, null);
        return numRowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        int numRowsUpdated = 0;
        switch (match) {
            case MOVIE:
            {
                numRowsUpdated = db.update(MovieEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (numRowsUpdated > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return 0;
    }
}
