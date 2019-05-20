package se.umu.oskar.interval.files;

import android.content.Context;
import android.content.res.Resources;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import se.umu.oskar.interval.R;

public class ExerciseCatalog {

    /**
     * File-spec is
     *  just write the name of the exercise one per line
     *   on exercise per line
     *
         Pull-ups
     */

    private static final String savePath = "exercises.txt";

    //this is public as to warn people that they are dealing with the direct reference
    public final ArrayList<String> exercises;
    private final Context context;

    public ExerciseCatalog(Context context) throws IOException {
        this.context = context;
        String content = Reader.readFileToString(context, savePath);

        if (content == null) {
            this.exercises = loadDefaults();
        } else {
            this.exercises = getExercises(content);
        }
    }

    public ExerciseCatalog(Context context, ArrayList<String> exerciseList) {
        this.context = context;
        this.exercises = exerciseList;
    }

    private ArrayList<String> loadDefaults() {
        ArrayList<String> exercises = new ArrayList<>();
        exercises.add(fromResources(R.string.pull_ups));
        exercises.add(fromResources(R.string.push_ups));
        exercises.add(fromResources(R.string.sit_ups));
        exercises.add(fromResources(R.string.high_jumps));
        exercises.add(fromResources(R.string.stretch));
        return exercises;
    }

    private String fromResources(int nameId) {
        Resources res = context.getResources();
        return res.getText(nameId).toString();
    }

    public void writeContentsToFile() throws IOException {
        FileOutputStream fop = context.openFileOutput(savePath, Context.MODE_PRIVATE);
        PrintWriter pw = new PrintWriter(fop);

        for (String name : exercises) {
            pw.append(name);
            pw.append('\n');
        }

        pw.close();
        fop.close();
    }

    static ArrayList<String> getExercises(String fileContents) {
        ArrayList<String> exercises = new ArrayList<>();
        String[] strings = fileContents.split("\n");

        for (String string : strings) {
            String line = string.trim();
            if (line.equals("")) continue;
            exercises.add(line);
        }

        return exercises;
    }
}
