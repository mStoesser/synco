package net.blauerfalke.synco.merge.field;

import net.blauerfalke.synco.merge.conflict.MergeConflictStrategy;
import net.blauerfalke.synco.model.Diff;
import net.blauerfalke.synco.model.SyncTriple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(JUnit4.class)
public class SimpleFieldMergeStrategyTest {

    private SimpleFieldMergeStrategy simpleFieldMergeStrategy = new SimpleFieldMergeStrategy();

    @Test
    public void testMergeFieldCallsMergeConflictStrategyOnConflict() {
        Diff<String> leftDiff = new Diff<>("a", "b");
        Diff<String> rightDiff = new Diff<>("a", "c");
        SyncTriple syncTriple = mock(SyncTriple.class);
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);
        Diff<?> expected = new Diff<>("a", "bc");
        doReturn(expected).when(mergeConflictStrategy).mergeField(any(), any(), any());

        Diff<?> result = simpleFieldMergeStrategy.mergeField(leftDiff, rightDiff, syncTriple, mergeConflictStrategy);

        assertEquals(expected, result);
        verify(mergeConflictStrategy).mergeField(leftDiff, rightDiff, syncTriple);
    }

    @Test
    public void testMergeFieldDirectlyReturnsDiffOnEquals() {
        Diff<String> leftDiff = new Diff<>("a", "d");
        Diff<String> rightDiff = new Diff<>("a", "d");

        Diff<?> result = simpleFieldMergeStrategy.mergeField(leftDiff, rightDiff, mock(SyncTriple.class), mock(MergeConflictStrategy.class));

        assertEquals(leftDiff, result);
    }

    @Test
    public void testMergeFieldDirectlyReturnsDiffOnNullEquals() {
        Diff<String> leftDiff = new Diff<>("a", null);
        Diff<String> rightDiff = new Diff<>("a", null);

        Diff<?> result = simpleFieldMergeStrategy.mergeField(leftDiff, rightDiff, mock(SyncTriple.class), mock(MergeConflictStrategy.class));

        assertEquals(leftDiff, result);
    }
}
