package se.umu.oskar.interval.files;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.List;

public class ExerciseCatalogTest {

    /**
     * File-spec is
     *
     * -- means new workout
     * The line after -- is info about the workout
     * "name" pauseTimeInSeconds nrOfRepetitions
     * Then until the next -- we list the exercises in the workout
     * name timeInSeconds colorAsHex
     */

    private static final String fileContents =
            "" +
                    "\"Pull-ups\" 120 888005\n" +
                    "\"Sit-ups\" 120 888005\n" +
                    "\"The idiot\" 120 888005\n" +
                    "\"Pull-ups\" 120 888005\n" +
                    "\"Pull-ups\" 120 887705\n" +
                    "\"Pull-ups\" 120 888005\n" +
                    "\"Sit-ups\" 3 877005\n" +
                    "\"Push-ups\" 120 828005\n" +
                    "\"Pull-ups\" 120 0\n";
    @Test
    public void test_has_correct_size() {
        List<String> names = ExerciseCatalog.getExercises(fileContents);
        assertEquals(9, names.size());
    }

    @Test
    public void hasCorrectName() {
        assertEquals("Pull-ups", getExercise(0));
        assertEquals("The idiot", getExercise(2));
    }

    private String getExercise(int index) {
        return ExerciseCatalog.getExercises(fileContents).get(index);
    }
}