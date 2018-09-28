package net.blauerfalke.synco.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class DiffTest {
    @Test
    public void testDiff() {
        Diff diff = new Diff<>("a", "b");
        assertEquals("a", diff.from);
        assertEquals("b", diff.to);
    }
}
