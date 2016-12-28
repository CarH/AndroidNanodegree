package com.ch.popularmovies.utilities;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by CarH on 22/11/2016.
 */

public class Utility {
    public static boolean isPortrait(Context context) {
        return context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT;
    }

    public static Bitmap getBitmapFromFilename(String filename) throws FileNotFoundException {
        Bitmap bitmap = null;
        File file = new File(filename);
        bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
        return bitmap;
    }
}
