package net.blauerfalke.synco.merge.field;


import net.blauerfalke.synco.merge.conflict.MergeConflictStrategy;
import net.blauerfalke.synco.model.Diff;
import net.blauerfalke.synco.model.SyncTriple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class ListFieldMergeStrategyTest {

    private static final String ID = "ID";

    private ListFieldMergeStrategy listFieldMergeStrategy = new ListFieldMergeStrategy();

    @Test(expected = IllegalArgumentException.class)
    public void testMergeFieldThrowsIllegalArgumentExceptionOnNoListType() {
        listFieldMergeStrategy.mergeField(new Diff<>(null, "string"), new Diff<>(null, "string"), mock(SyncTriple.class), mock(MergeConflictStrategy.class));
    }

    @Test
    public void testAddLeft() {
        List<String> baseList = Arrays.asList("base");
        List<String> leftList = Arrays.asList("base", "local");
        List<String> rightList = Arrays.asList("base");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, leftList), new Diff<>(baseList, rightList), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertEquals(leftList, result.to);
        verifyNoMoreInteractions(mergeConflictStrategy);
    }

    @Test
    public void testAddRight() {
        List<String> baseList = Arrays.asList("base");
        List<String> leftList = Arrays.asList("base");
        List<String> rightList = Arrays.asList("base", "right");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, leftList), new Diff<>(baseList, rightList), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertEquals(rightList, result.to);
        verifyNoMoreInteractions(mergeConflictStrategy);
    }

    @Test
    public void testRemoveLeft() {
        List<String> baseList = Arrays.asList("base", "list");
        List<String> leftList = Arrays.asList("list");
        List<String> rightList = Arrays.asList("base", "list");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, leftList), new Diff<>(baseList, rightList), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertEquals(leftList, result.to);
        verifyNoMoreInteractions(mergeConflictStrategy);
    }

    @Test
    public void testRemoveRight() {
        List<String> baseList = Arrays.asList("base", "list");
        List<String> leftList = Arrays.asList("base", "list");
        List<String> rightList = Arrays.asList("list");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, leftList), new Diff<>(baseList, rightList), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertEquals(rightList, result.to);
        verifyNoMoreInteractions(mergeConflictStrategy);
    }

    @Test
    public void testRemoveLeftAddRight() {
        List<String> baseList = Arrays.asList("base", "list");
        List<String> leftList = Arrays.asList("list");
        List<String> rightList = Arrays.asList("base", "list", "cool");
        List<String> expectedList = Arrays.asList("list", "cool");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, leftList), new Diff<>(baseList, rightList), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertEquals(expectedList, result.to);
        verifyNoMoreInteractions(mergeConflictStrategy);
    }

    @Test
    public void testRemoveRightAddLeft() {
        List<String> baseList = Arrays.asList("base", "list");
        List<String> leftList = Arrays.asList("base", "list", "some");
        List<String> rightList = Arrays.asList("base");
        List<String> expectedList = Arrays.asList("base", "some");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, leftList), new Diff<>(baseList, rightList), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertEquals(expectedList, result.to);
        verifyNoMoreInteractions(mergeConflictStrategy);
    }

    @Test
    public void testRemoveRightAddLeftConflictSolveLeft() {
        List<String> baseList = Arrays.asList("base", "list", "some", "cool");
        List<String> leftList = Arrays.asList("base", "list", "some", "cool", "cool");
        List<String> rightList = Arrays.asList("base", "list", "some");
        List<String> expectedList = Arrays.asList("base", "list", "some", "cool", "cool");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);
        when(mergeConflictStrategy.mergeField(any(Diff.class), any(Diff.class), any(SyncTriple.class))).thenAnswer((invocationOnMock -> invocationOnMock.getArgument(0)));

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, leftList), new Diff<>(baseList, rightList), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertEquals(expectedList, result.to);
        verify(mergeConflictStrategy, times(2)).mergeField(any(Diff.class), any(Diff.class), any(SyncTriple.class));
    }

    @Test
    public void testRemoveRightAddLeftConflictSolveRight() {
        List<String> baseList = Arrays.asList("base", "list", "some", "cool");
        List<String> leftList = Arrays.asList("base", "list", "some", "cool", "cool");
        List<String> rightList = Arrays.asList("base", "list", "some");
        List<String> expectedList = Arrays.asList("base", "list", "some");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);
        when(mergeConflictStrategy.mergeField(any(Diff.class), any(Diff.class), any(SyncTriple.class))).thenAnswer((invocationOnMock -> invocationOnMock.getArgument(1)));

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, leftList), new Diff<>(baseList, rightList), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertEquals(expectedList, result.to);
        verify(mergeConflictStrategy, times(2)).mergeField(any(Diff.class), any(Diff.class), any(SyncTriple.class));
    }

    @Test
    public void testRemoveRightAdd2LeftConflictSolveLeft() {
        List<String> baseList = Arrays.asList("base", "list", "some", "cool");
        List<String> leftList = Arrays.asList("base", "list", "some", "cool", "cool", "cool");
        List<String> rightList = Arrays.asList("base", "list", "some");
        List<String> expectedList = Arrays.asList("base", "list", "some", "cool", "cool", "cool");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);
        when(mergeConflictStrategy.mergeField(any(Diff.class), any(Diff.class), any(SyncTriple.class))).thenAnswer((invocationOnMock -> invocationOnMock.getArgument(0)));

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, leftList), new Diff<>(baseList, rightList), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertEquals(expectedList, result.to);
        verify(mergeConflictStrategy, times(3)).mergeField(any(Diff.class), any(Diff.class), any(SyncTriple.class));
    }

    @Test
    public void testInsertRightRemoveLeftConflictSolveLeft() {
        List<String> baseList = Arrays.asList("base", "list", "some", "cool");
        List<String> leftList = Arrays.asList("base", "list", "some");
        List<String> rightList = Arrays.asList("base", "list", "some", "cool", "cool");
        List<String> expectedList = Arrays.asList("base", "list", "some");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);
        when(mergeConflictStrategy.mergeField(any(Diff.class), any(Diff.class), any(SyncTriple.class))).thenAnswer((invocationOnMock -> invocationOnMock.getArgument(0)));

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, leftList), new Diff<>(baseList, rightList), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertEquals(expectedList, result.to);
        verify(mergeConflictStrategy, times(2)).mergeField(any(Diff.class), any(Diff.class), any(SyncTriple.class));
    }

    @Test
    public void testInsertRightRemoveLeftConflictSolveRight() {
        List<String> baseList = Arrays.asList("base", "list", "some", "cool");
        List<String> leftList = Arrays.asList("base", "list", "some");
        List<String> rightList = Arrays.asList("base", "list", "some", "cool", "cool");
        List<String> expectedList = Arrays.asList("base", "list", "some", "cool", "cool");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);
        when(mergeConflictStrategy.mergeField(any(Diff.class), any(Diff.class), any(SyncTriple.class))).thenAnswer((invocationOnMock -> invocationOnMock.getArgument(1)));

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, leftList), new Diff<>(baseList, rightList), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertEquals(expectedList, result.to);
        verify(mergeConflictStrategy, times(2)).mergeField(any(Diff.class), any(Diff.class), any(SyncTriple.class));
    }

    @Test
    public void testInsertRemoveRightAndInsertRemoveLeftNoConflict() {
        List<String> baseList = Arrays.asList("base", "list", "some", "cool", "stuff", "insert", "me");
        List<String> leftList = Arrays.asList("base", "list", "some", "stuff", "insert", "me", "more"); // removed cool, add more
        List<String> rightList = Arrays.asList("base", "list", "some", "cool", "insert", "me", "blah"); // removed stuff, add blah
        List<String> expectedList = Arrays.asList("base", "list", "some", "insert", "me", "more", "blah");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, leftList), new Diff<>(baseList, rightList), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertEquals(expectedList, result.to);
        verifyNoMoreInteractions(mergeConflictStrategy);
    }

    @Test
    public void testRemove2RightAndRemove2LeftNoConflict() {
        List<String> baseList = Arrays.asList("base", "list", "some", "some", "stuff", "insert", "me");
        List<String> leftList = Arrays.asList("base", "list", "stuff", "insert", "me");
        List<String> rightList = Arrays.asList("base", "list", "stuff", "insert", "me");
        List<String> expectedList = Arrays.asList("base", "list", "stuff", "insert", "me");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, leftList), new Diff<>(baseList, rightList), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertEquals(expectedList, result.to);
        verifyNoMoreInteractions(mergeConflictStrategy);
    }

    @Test
    public void testRemove2LeftAndInsert1RightConflictSolveLeft() {
        List<String> baseList = Arrays.asList("base", "list", "some", "some", "stuff", "insert", "me");
        List<String> leftList = Arrays.asList("base", "list", "stuff", "insert", "me");
        List<String> rightList = Arrays.asList("base", "list", "some", "some", "stuff", "some", "insert", "me");
        List<String> expectedList = Arrays.asList("base", "list", "stuff", "insert", "me");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);
        when(mergeConflictStrategy.mergeField(any(Diff.class), any(Diff.class), any(SyncTriple.class))).thenAnswer((invocationOnMock -> invocationOnMock.getArgument(0)));

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, leftList), new Diff<>(baseList, rightList), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertEquals(expectedList, result.to);
        verify(mergeConflictStrategy, times(3)).mergeField(any(Diff.class), any(Diff.class), any(SyncTriple.class));
    }

    @Test
    public void testWithLeftRightNull() {
        List<String> baseList = Arrays.asList("base");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, null), new Diff<>(baseList, null), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertNull(result.to);
        verifyNoMoreInteractions(mergeConflictStrategy);
    }

    @Test
    public void testWithLeftRightFromToNull() {
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(null, null), new Diff<>(null, null), mock(SyncTriple.class), mergeConflictStrategy);

        assertNull(result.from);
        assertNull(result.to);
        verifyNoMoreInteractions(mergeConflictStrategy);
    }

    @Test
    public void testWithLeftToNullNoConflict() {
        List<String> baseList = Arrays.asList("base");
        List<String> leftList = Arrays.asList("base");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, leftList), new Diff<>(baseList, null), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertNull(result.to);
        verifyNoMoreInteractions(mergeConflictStrategy);
    }

    @Test
    public void testWithRightToNullNoConflict() {
        List<String> baseList = Arrays.asList("base");
        List<String> rightList = Arrays.asList("base");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, null), new Diff<>(baseList, rightList), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertNull(result.to);
        verifyNoMoreInteractions(mergeConflictStrategy);
    }

    @Test
    public void testWithLeftAddOneAndRightToNullConflictSolveLeft() {
        List<String> baseList = Arrays.asList("base");
        List<String> leftList = Arrays.asList("base", "add");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);
        when(mergeConflictStrategy.mergeField(any(Diff.class), any(Diff.class), any(SyncTriple.class))).thenAnswer((invocationOnMock -> invocationOnMock.getArgument(0)));

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, leftList), new Diff<>(baseList, null), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertEquals(leftList, result.to);
        verify(mergeConflictStrategy, times(1)).mergeField(any(Diff.class), any(Diff.class), any(SyncTriple.class));
    }

    @Test
    public void testWithLeftAddOneAndRightToNullConflictSolveRight() {
        List<String> baseList = Arrays.asList("base");
        List<String> leftList = Arrays.asList("base", "add");
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);
        when(mergeConflictStrategy.mergeField(any(Diff.class), any(Diff.class), any(SyncTriple.class))).thenAnswer((invocationOnMock -> invocationOnMock.getArgument(1)));

        Diff<?> result = listFieldMergeStrategy.mergeField(new Diff<>(baseList, leftList), new Diff<>(baseList, null), mock(SyncTriple.class), mergeConflictStrategy);

        assertEquals(baseList, result.from);
        assertNull(result.to);
        verify(mergeConflictStrategy, times(1)).mergeField(any(Diff.class), any(Diff.class), any(SyncTriple.class));
    }
}