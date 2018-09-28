package net.blauerfalke.synco.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(JUnit4.class)
public class SyncTripleTest {

    @Test
    public void testSyncTriple() {
        Syncable base = mock(Syncable.class);
        Syncable left = mock(Syncable.class);
        Syncable right = mock(Syncable.class);
        SyncTriple syncTriple = new SyncTriple(base, left, right);
        assertEquals(base, syncTriple.base);
        assertEquals(left, syncTriple.left);
        assertEquals(right, syncTriple.right);
    }

}
