package se.umu.oskar.interval.files;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class 'remembers' the last used time for an block in a workout
 * So if the user adds a 40sec long push-up then the next time they add a push-up it will default
 * to 40s as well, convenient.
 */
public class BlockTimes {
    private static final String blockTimes = "BlockTimes";
    private final SharedPreferences sharedpreferences;
    private int defaultTime;
    private final String pauseName = "pause\npause"; //not possible for block to be named this
    private final int defaultPauseTime;

    public BlockTimes(Context context, int defaultTime, int defaultPauseTime) {
        this.defaultTime = defaultTime;
        this.defaultPauseTime = defaultPauseTime;
        sharedpreferences = context.getSharedPreferences(blockTimes, Context.MODE_PRIVATE);
    }

    public synchronized int pauseTime() {
        return sharedpreferences.getInt(pauseName, defaultPauseTime);
    }

    public synchronized void setPauseTime(int i) {
        sharedpreferences.edit().putInt(pauseName, i).apply();
    }

    public synchronized int getOrDefault(String nameOfBlock) {
        return sharedpreferences.getInt(nameOfBlock, defaultTime);
    }

    public synchronized void setTime(String nameOfBlock, int time) {
        sharedpreferences.edit().putInt(nameOfBlock, time).apply();
    }

    public synchronized void setDefaultTime(int defaultTime) {
        this.defaultTime = defaultTime;
    }
}
