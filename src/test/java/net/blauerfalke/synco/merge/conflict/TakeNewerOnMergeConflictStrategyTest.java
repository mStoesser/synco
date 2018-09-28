package net.blauerfalke.synco.merge.conflict;

import net.blauerfalke.synco.model.Diff;
import net.blauerfalke.synco.model.SyncTriple;
import net.blauerfalke.synco.model.Syncable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(JUnit4.class)
public class TakeNewerOnMergeConflictStrategyTest {

    private TakeNewerOnMergeConflictStrategy takeNewerOnMergeConflictStrategy = new TakeNewerOnMergeConflictStrategy();

    @Test
    public void testMergeFieldReturnsLeftDiffOnLeftNewer() {
        Diff leftDiff = new Diff("a", "b");
        Diff rightDiff = new Diff("a", "c");
        Syncable left = mock(Syncable.class);
        when(left.getUpdated()).thenReturn(100L);
        Syncable right = mock(Syncable.class);
        when(right.getUpdated()).thenReturn(200L);
        SyncTriple syncTriple = new SyncTriple(mock(Syncable.class), left, right);

        Diff<?> result = takeNewerOnMergeConflictStrategy.mergeField(leftDiff, rightDiff, syncTriple);

        assertEquals(rightDiff, result);
    }

    @Test
    public void testMergeFieldReturnsRightDiffOnRightNewer() {
        Diff leftDiff = new Diff("a", "b");
        Diff rightDiff = new Diff("a", "c");
        Syncable left = mock(Syncable.class);
        when(left.getUpdated()).thenReturn(200L);
        Syncable right = mock(Syncable.class);
        when(right.getUpdated()).thenReturn(100L);
        SyncTriple syncTriple = new SyncTriple(mock(Syncable.class), left, right);

        Diff<?> result = takeNewerOnMergeConflictStrategy.mergeField(leftDiff, rightDiff, syncTriple);

        assertEquals(leftDiff, result);
    }

    @Test
    public void testMergeFieldReturnsLeftOnEquals() {
        Diff leftDiff = new Diff("a", "b");
        Diff rightDiff = new Diff("a", "c");
        Syncable left = mock(Syncable.class);
        when(left.getUpdated()).thenReturn(500L);
        Syncable right = mock(Syncable.class);
        when(right.getUpdated()).thenReturn(500L);
        SyncTriple syncTriple = new SyncTriple(mock(Syncable.class), left, right);

        Diff<?> result = takeNewerOnMergeConflictStrategy.mergeField(leftDiff, rightDiff, syncTriple);

        assertEquals(leftDiff, result);
    }

}
