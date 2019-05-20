package se.umu.oskar.interval.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v7.widget.LinearLayoutManager;

import se.umu.oskar.interval.R;


public class ViewUtilities {
    public static @ColorInt int resToColor(Resources resources, int colorResource) {
        return resources.getColor(colorResource, null);
    }

    public static @ColorInt int darken(int color, float lightMultiplier) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= lightMultiplier;
        return Color.HSVToColor(hsv);
    }

    public static String timeToString(Resources resources, int time) {
        int seconds = time % 60;
        int minutes = time / 60; //integer division

        final String timeString;

        if (seconds != 0 && minutes != 0) {
            timeString = String.format(resources.getString(R.string.time_format), minutes, seconds);
        } else if (minutes != 0) {
            timeString = String.format(resources.getString(R.string.time_format_minutes_only), minutes);
        } else {
            timeString = String.format(resources.getString(R.string.time_format_seconds_only), seconds);
        }
        return timeString;
    }

    public static LinearLayoutManager verticalLayoutManager(Context context) {
        return new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
    }
}
