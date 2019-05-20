package se.umu.oskar.interval.files;

import static org.junit.Assert.*;
import org.junit.Test;

import se.umu.oskar.interval.model.Block;
import se.umu.oskar.interval.model.Pause;

public class BlockTest {
    @Test
    public void should_equals_after_copy() {
        Block before = new Block("gobbeli", 3, 5);
        Block after = before.copy();

        assertTrue(before.equals(after));
    }

    @Test
    public void should_have_same_information_after_copy() {
        Block before = new Block("gobbeli", 3, 5);
        Block after = before.copy();

        assertEquals(before.color() , after.color());
        assertEquals(before.name() , after.name());
        assertEquals(before.timeInSeconds() , after.timeInSeconds());
        assertEquals(before.id() , after.id());
    }

    @Test
    public void should_have_the_same_information_after_transformed_to_string_and_back() {
        Block before = new Block("hello", 5, 5);

        String line = before.blockToLine();

        Block after = Block.lineToBlock(line);

        assertTrue(after != null);
        assertEquals(before.name(), after.name());
        assertEquals(before.timeInSeconds(), after.timeInSeconds());
        assertEquals(before.isPause(), after.isPause());
        assertFalse(after.isPause());
        assertEquals(before.color(), after.color());
    }

    @Test
    public void pause_should_have_the_same_information_after_transformed_to_string_and_back() {
        Block before = new Pause("hello", 5, 5);

        String line = before.blockToLine();

        Block after = Block.lineToBlock(line);

        assertTrue(after != null);
        assertEquals(before.name(), after.name());
        assertEquals(before.timeInSeconds(), after.timeInSeconds());
        assertEquals(before.isPause(), after.isPause());
        assertTrue(after.isPause());
        assertEquals(before.color(), after.color());
    }
}
