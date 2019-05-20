package se.umu.oskar.interval;

import android.os.Parcel;

import org.junit.Test;

import se.umu.oskar.interval.model.Block;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class BlockTest extends ParcelTest{

    @Test
    public void should_have_the_same_parameters_including_id() {
        Block before = new Block("hello", 5, 12);

        Block after = fromParcel(makeParcel(before));

        assertEquals(before.id(), after.id());
        assertEquals(before.name(), after.name());
        assertEquals(before.color(), after.color());
    }

    @Test
    public void should_equal_after_parcelable() {
        Block before = new Block("hello", 5, 12);
        Block after = fromParcel(makeParcel(before));
        assertTrue(before.equals(after));
    }

    private Block fromParcel(final Parcel parcel) {
        parcel.setDataPosition(0);
        return Block.CREATOR.createFromParcel(parcel);
    }

}
