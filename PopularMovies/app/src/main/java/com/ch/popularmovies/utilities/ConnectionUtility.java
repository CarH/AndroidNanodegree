package com.ch.popularmovies.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created on 06/11/2016.
 */

public class ConnectionUtility {
    public static boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static void printNotConnectedMessage(Context context) {
        Toast.makeText(context, "No internet connection :(", Toast.LENGTH_LONG).show();
    }
}
