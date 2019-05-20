package se.umu.oskar.interval;

import android.content.Context;
import android.os.Parcel;
import android.support.test.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import se.umu.oskar.interval.model.Block;
import se.umu.oskar.interval.model.Pause;
import se.umu.oskar.interval.model.Workout;

public class WorkoutTest extends ParcelTest{

    private final Context context = InstrumentationRegistry.getTargetContext();

    @Test
    public void should_not_insert_pauses() {
        Workout workout = new Workout("Work");
        workout.setPauseBetween(5);
        workout.setNrOfRepetitions(3);

        workout.add(new Block("hello", 5, 0));
        workout.add(new Pause(5, context));

        List<Block> blocks = workout.blocksWithPausesAndRepetitions(context);
        Assert.assertEquals(blocks.size(), 6);

        Assert.assertFalse(blocks.get(0).isPause());
        Assert.assertTrue(blocks.get(1).isPause());

        Assert.assertFalse(blocks.get(2).isPause());
        Assert.assertTrue(blocks.get(3).isPause());

        Assert.assertFalse(blocks.get(4).isPause());
        Assert.assertTrue(blocks.get(5).isPause());

    }

    @Test
    public void should_insert_pause_only_between_the_two_blocks() {
        Workout workout = new Workout("Work");
        workout.setPauseBetween(5);
        workout.setNrOfRepetitions(2);

        workout.add(new Block("hello", 5, 0));
        workout.add(new Block("hello", 5, 0));
        workout.add(new Pause(20, context));

        List<Block> blocks = workout.blocksWithPausesAndRepetitions(context);
        Assert.assertEquals(blocks.size(), 8);

        Assert.assertFalse(blocks.get(0).isPause()); // not pause
        Assert.assertTrue(blocks.get(1).isPause()); // pause
        Assert.assertFalse(blocks.get(2).isPause()); // block
        Assert.assertTrue(blocks.get(3).isPause()); // pause

        Assert.assertFalse(blocks.get(4).isPause()); // not pause
        Assert.assertTrue(blocks.get(5).isPause()); // pause
        Assert.assertFalse(blocks.get(6).isPause()); // not pause
        Assert.assertTrue(blocks.get(7).isPause()); // pause


        //checking length of inserted pause
        Assert.assertEquals(workout.pauseBetween(), blocks.get(1).timeInSeconds());
    }

    @Test
    public void workoutHasSameEop() {
        final Workout before = getWorkout();

        final Parcel parcel = makeParcel(before);

        final int eopBefore = parcel.dataPosition();
        fromParcel(parcel);

        assertSame(eopBefore, parcel.dataPosition());
    }

    @Test
    public void workoutIsTheSame() {
        Workout before = getWorkout();
        Workout after = fromParcel(makeParcel(before));

        Assert.assertEquals(after.withRepetitions().size(), before.withRepetitions().size());
    }

    private Workout getWorkout() {
        Workout workout = new Workout("test");
        workout.add(new Block("test", 23, 0));
        return workout;
    }

    private Workout fromParcel(final Parcel parcel) {
        parcel.setDataPosition(0);
        return Workout.CREATOR.createFromParcel(parcel);
    }

}
