package se.umu.oskar.interval.model;

import android.content.Context;

import se.umu.oskar.interval.R;

public class Pause extends Block {
    public Pause(int timeInSeconds, Context context) {
        this(context.getResources().getText(R.string.pause).toString()
                , timeInSeconds
                , context.getColor(R.color.pause));
        setIsPause(true);
    }

    public Pause(String name, int timeInSeconds, int color) {
        super(name, timeInSeconds, color);
        setIsPause(true);
    }
}
