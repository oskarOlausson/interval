package se.umu.oskar.interval.files;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import se.umu.oskar.interval.R;
import se.umu.oskar.interval.helpers.Tags;
import se.umu.oskar.interval.model.Block;
import se.umu.oskar.interval.model.Workout;

public class WorkoutCatalog {

    /**
     * File-spec is
     *
     * -- means new workout
     * The line after -- is info about the workout
     * "name" pauseTimeInSeconds nrOfRepetitions
     * Then until the next -- we list the exercises in the workout
     * name timeInSeconds colorAsHex
     *
     * example (-- at end of file is optional and is prohibited at start of file)

         "My cool workout" 10 3
         "Pull-ups" 120 888005
         "Sit-ups" 120 888005
         "Push-ups" 120 888005
         "Pull-ups" 120 888005
         "Pull-ups" 120 887705
         --
         "Workout 2" 4 1
         "Pull-ups" 120 888005
         "Sit-ups" 120 877005
         "Push-ups" 120 828005
         "Pull-ups" 120 888001
         "Pull-ups" 120 888007
         --

     */

    private static final String workoutSeparator = "--";
    private static final String defaultFilePath = "workouts.txt";

    //this is public as to warn people that they are dealing with the direct reference
    public final ArrayList<Workout> workouts;
    private final Context context;

    public WorkoutCatalog(Context context) throws IOException {
        this.context = context;
        String content = Reader.readFileToString(context, defaultFilePath);
        workouts = getWorkoutsOrExample(content);
    }

    public WorkoutCatalog(Context context, Workout current) throws IOException {
        this.context = context;
        String content = Reader.readFileToString(context, defaultFilePath);

        workouts = getWorkoutsOrExample(content);

        // so we are not saving it to file multiple times
        removeCurrent(current);
        workouts.add(current);
    }

    private ArrayList<Workout> getWorkoutsOrExample(String content) {
        if (content == null) {
            ArrayList<Workout> workouts;
            workouts = new ArrayList<>();
            workouts.add(exampleWorkout(context.getResources()));
            return workouts;
        } else {
            return getWorkouts(content);
        }
    }

    private void removeCurrent(Workout current) {
        Iterator<Workout> it = workouts.iterator();
        while (it.hasNext()) {
            Workout wp = it.next();

            if (wp.name().equals(current.name())) {
                it.remove();
            }
        }
    }

    public void writeContentsToFile() throws IOException {
        FileOutputStream fop = context.openFileOutput(defaultFilePath, Context.MODE_PRIVATE);
        PrintWriter pw = new PrintWriter(fop);

        for (Workout wp : workouts) {
            pw.append(formatWorkout(wp));
        }

        pw.close();
        fop.close();
    }

    public static String formatWorkout(Workout w) {
        StringBuilder sb = new StringBuilder();
        sb.append(w.toHeaderLine());

        for (Block b : w.rawBlocks()) {
            sb.append(b.blockToLine());
        }

        sb.append(workoutSeparator);
        sb.append('\n');
        return sb.toString();
    }

    public static Workout consumeNextWorkout(Queue<String> lines) {
        if (lines.isEmpty()) {
            return null;
        }

        Workout workout = null;
        String line;
        do {
            line = lines.poll().trim();
            if (!line.startsWith(workoutSeparator)) {
                if (workout == null) {
                    workout = Workout.fromLine(line);
                    if (workout == null) {
                        Log.w(Tags.oskarTag, "Could not load workout from line: " + line);
                    }
                } else {
                    Block block = Block.lineToBlock(line);

                    if (block != null) {
                        workout.add(block);
                    } else {
                        Log.w(Tags.oskarTag, "Could not load block from line: " + line);
                    }
                }
            }
        } while (!line.startsWith(workoutSeparator) && !lines.isEmpty());

        return workout;
    }
    
    private static ArrayList<Workout> getWorkouts(String fileContents) {
        ArrayList<Workout> workouts = new ArrayList<>();
        String[] strings = fileContents.split("\n");

        Queue<String> lineQueue = new LinkedList<>(Arrays.asList(strings));

        while(!lineQueue.isEmpty()) {
            Workout workout = consumeNextWorkout(lineQueue);

            if (workout != null) {
                workouts.add(workout);
            } else {
                Log.w(Tags.oskarTag, "Could not parseFromQR workout");
            }
        }

        return workouts;
    }

    public boolean hasName(String name) {
        for (Workout wp : workouts) {
            if (wp.name().equals(name)) return true;
        }

        return false;
    }

    private static Workout exampleWorkout (Resources resources) {
        Workout w = new Workout(resources.getString(R.string.example));
        int color = resources.getColor(R.color.item_1, null);
        int color2 = resources.getColor(R.color.item_4, null);
        int color3 = resources.getColor(R.color.item_3, null);

        w.setPauseBetween(5);
        w.setNrOfRepetitions(2);

        w.add(new Block(resources.getText(R.string.pull_ups).toString(), 30, color));
        w.add(new Block(resources.getText(R.string.push_ups).toString(), 30, color2));
        w.add(new Block(resources.getText(R.string.sit_ups).toString(), 30, color3));

        return w;
    }

    public void reload() throws IOException {
        String content = Reader.readFileToString(context, defaultFilePath);
        workouts.clear();
        workouts.addAll(getWorkoutsOrExample(content));
    }
}
