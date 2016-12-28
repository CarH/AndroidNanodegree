package com.ch.popularmovies.entities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by CarH on 12/12/2016.
 */

public class Movie implements Parcelable {
    public String title;
    public String originalTitle;
    public String synopsis;
    public String releaseDate;
    public String popularity;
    public String voteCount;
    public String userRating;
    public String posterUrl;
    public long tmdbMovieId;

    public Movie(){
    }

    Movie(Parcel in) {
        title = in.readString();
        originalTitle = in.readString();
        synopsis = in.readString();
        releaseDate = in.readString();
        popularity = in.readString();
        voteCount = in.readString();
        userRating = in.readString();
        posterUrl = in.readString();
        tmdbMovieId = in.readLong();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
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
        parcel.writeString(synopsis);
        parcel.writeString(releaseDate);
        parcel.writeString(popularity);
        parcel.writeString(voteCount);
        parcel.writeString(userRating);
        parcel.writeString(posterUrl);
        parcel.writeLong(tmdbMovieId);
    }
}
