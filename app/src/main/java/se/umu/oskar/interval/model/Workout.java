package se.umu.oskar.interval.model;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.vision.barcode.Barcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.umu.oskar.interval.R;
import se.umu.oskar.interval.files.WorkoutCatalog;

public class Workout implements Parcelable {
    public static final String topLineInQr = "Interval\n";

    private ArrayList<Block> blocks = new ArrayList<>();
    private int defaultPauseSeconds = 0;
    private int nrOfRepetitions = 1;
    private String name;


    public Workout(String name) {
        this.name = name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public void add(Block block) {
        blocks.add(block);
    }

    public void remove(int index) {
        blocks.remove(index);
    }

    public int size() {
        return blocks.size();
    }

    public Block atPosition(int position) {
        return blocks.get(position);
    }

    public int indexOf(Block b) {
        return blocks.indexOf(b);
    }

    public void move(int fromPosition, int toPosition) {
        Block toMove = atPosition(fromPosition);
        blocks.remove(toMove);
        blocks.add(toPosition, toMove);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(defaultPauseSeconds);
        parcel.writeInt(nrOfRepetitions);
        parcel.writeTypedList(blocks);
    }

    public static final Parcelable.Creator<Workout> CREATOR = new Parcelable.Creator<Workout>() {
        @Override
        public Workout createFromParcel(Parcel parcel) {
            Workout workout = new Workout(parcel.readString());
            workout.defaultPauseSeconds = parcel.readInt();
            workout.nrOfRepetitions = parcel.readInt();
            workout.blocks = parcel.createTypedArrayList(Block.CREATOR);
            return workout;
        }

        @Override
        public Workout[] newArray(int i) {
            return new Workout[0];
        }
    };

    public int totalTimeInSeconds() {
        List<Block> blocksWithRepetitionsAndPause = blocksWithPausesAndRepetitions("", 0);
        int sum = 0;

        for (Block block : blocksWithRepetitionsAndPause) {
            sum += block.timeInSeconds();
        }

        return sum;
    }

    public void setPauseBetween(int seconds) {
        defaultPauseSeconds = seconds;
    }

    public int pauseBetween() {
        return defaultPauseSeconds;
    }

    public ArrayList<Block> withRepetitions() {
        ArrayList<Block> allBlocks = new ArrayList<>();

        for (int i = 0; i < nrOfRepetitions; i++) {
            for (Block block : blocks) {
                allBlocks.add(block.copy());
            }
        }

        return allBlocks;
    }

    public void setNrOfRepetitions(int repetitions) {
        nrOfRepetitions = repetitions;
    }

    public int repetitions() {
        return nrOfRepetitions;
    }

    public String name() {
        return name;
    }

    public ArrayList<Block> rawBlocks() {
        return blocks;
    }

    private ArrayList<Block> blocksWithPausesAndRepetitions(String pauseName, int color) {
        ArrayList<Block> withRepetitions = withRepetitions();

        if (withRepetitions.isEmpty()) return withRepetitions;

        int pauseTime = pauseBetween();
        if (pauseTime <= 0) return withRepetitions;

        ArrayList<Block> withPauses = new ArrayList<>();

        for (int i = 0; i < withRepetitions.size() - 1; i++) {
            Block block = withRepetitions.get(i);
            withPauses.add(block);

            if (!block.isPause()) {
                Block next = withRepetitions.get(i + 1);
                if (!next.isPause()) {
                    Block pause = new Pause(pauseName, pauseTime, color);
                    withPauses.add(pause);
                }
            }
        }

        //no pause after last one
        withPauses.add(withRepetitions.get(withRepetitions.size() - 1));

        return withPauses;
    }

    public ArrayList<Block> blocksWithPausesAndRepetitions(Context context) {
        Resources res = context.getResources();
        return blocksWithPausesAndRepetitions(res.getString(R.string.pause), res.getColor(R.color.pause, null));
    }

    public static Workout parseFromQR(Barcode barcode) {
        String[] lines = barcode.rawValue.split("\n");
        Queue<String> lineQueue = new LinkedList<>();

        //skipping first line where it only says Interval
        lineQueue.addAll(Arrays.asList(lines).subList(1, lines.length));

        return WorkoutCatalog.consumeNextWorkout(lineQueue);
    }

    private static final Pattern pattern = Pattern.compile("\"([^\"]*)\" (-?\\d+) (-?\\d+)");

    public static Workout fromLine(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String name = matcher.group(1);
            Workout workout = new Workout(name);

            workout.setNrOfRepetitions(Integer.parseInt(matcher.group(2)));
            workout.setPauseBetween(Integer.parseInt(matcher.group(3)));

            return workout;
        } else return null;
    }

    public String toHeaderLine() {
        String quote = "\"";
        String space = " ";

        return quote + name + quote + space
                + nrOfRepetitions + space
                + defaultPauseSeconds + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Workout workout = (Workout) o;
        return name.equals(workout.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
