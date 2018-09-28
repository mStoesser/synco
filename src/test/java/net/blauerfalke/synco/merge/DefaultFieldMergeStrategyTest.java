package net.blauerfalke.synco.merge;


import net.blauerfalke.synco.Sync;
import net.blauerfalke.synco.conf.SyncConfiguration;
import net.blauerfalke.synco.merge.conflict.TakeNewerOnMergeConflictStrategy;
import net.blauerfalke.synco.merge.field.SimpleFieldMergeStrategy;
import net.blauerfalke.synco.model.SyncObject;
import net.blauerfalke.synco.model.SyncTriple;
import net.blauerfalke.synco.model.Syncable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(JUnit4.class)
public class DefaultFieldMergeStrategyTest {

    public static final String ID = "ID";

    private DefaultFieldMergeStrategy defaultFieldMergeStrategy = new DefaultFieldMergeStrategy();

    @Test
    public void testMergeOnlyLeftChanges() {
        Sync sync = mock(Sync.class);
        when(sync.cloneSyncable(any(Syncable.class))).then(invocationOnMock -> invocationOnMock.getArgument(0));
        when(sync.getSyncConfiguration()).thenReturn(new SyncConfiguration());
        SyncObject base = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        SyncObject left = new SyncObject(ID, "left", 1L, 1.5d, 100L, false);
        SyncObject right = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        SyncTriple syncTriple = new SyncTriple(base, left, right);

        Syncable result = defaultFieldMergeStrategy.merge(sync, syncTriple);

        assertTrue(result instanceof SyncObject);
        assertEquals(left, result);
        verify(sync).cloneSyncable(base);
    }

    @Test
    public void testMergeOnlyRightChanges() {
        Sync sync = mock(Sync.class);
        when(sync.cloneSyncable(any(Syncable.class))).then(invocationOnMock -> invocationOnMock.getArgument(0));
        when(sync.getSyncConfiguration()).thenReturn(new SyncConfiguration());
        SyncObject base = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        SyncObject left = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        SyncObject right = new SyncObject(ID, "right", 1L, 2.5d, 300L, false);
        SyncTriple syncTriple = new SyncTriple(base, left, right);

        Syncable result = defaultFieldMergeStrategy.merge(sync, syncTriple);

        assertTrue(result instanceof SyncObject);
        assertEquals(right, result);
        verify(sync).cloneSyncable(base);
    }

    @Test
    public void testMergeLeftAndRightChanges() {
        Sync sync = mock(Sync.class);
        when(sync.cloneSyncable(any(Syncable.class))).then(invocationOnMock -> invocationOnMock.getArgument(0));
        when(sync.getSyncConfiguration()).thenReturn(new SyncConfiguration());
        SyncObject base = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        SyncObject left = new SyncObject(ID, "base", 1L, 3.5d, 100L, false);
        SyncObject right = new SyncObject(ID, "right", 1L, 1.5d, 100L, false);
        SyncObject expected = new SyncObject(ID, "right", 1L, 3.5d, 100L, false);
        SyncTriple syncTriple = new SyncTriple(base, left, right);

        Syncable result = defaultFieldMergeStrategy.merge(sync, syncTriple);

        assertTrue(result instanceof SyncObject);
        assertNotEquals(right, result);
        assertNotEquals(left, result);
        assertEquals(expected, result);
        verify(sync).cloneSyncable(base);
    }

    @Test
    public void testMergeLeftAndRightChangesConflicted() {
        Sync sync = mock(Sync.class);
        when(sync.cloneSyncable(any(Syncable.class))).then(invocationOnMock -> invocationOnMock.getArgument(0));
        when(sync.getSyncConfiguration()).thenReturn(new SyncConfiguration());
        when(sync.getMergeConflictStrategy(any(Class.class), anyString(), any(Class.class))).thenReturn(new TakeNewerOnMergeConflictStrategy());
        when(sync.getFieldMergeStrategy(any(Class.class), anyString(), any(Class.class))).thenReturn(new SimpleFieldMergeStrategy());
        SyncObject base = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        SyncObject left = new SyncObject(ID, "left", 2L, 3.5d, 200L, false);
        SyncObject right = new SyncObject(ID, "right", 1L, 5.5d, 300L, false);
        SyncObject expected = new SyncObject(ID, "right", 2L, 5.5d, 300L, false);
        SyncTriple syncTriple = new SyncTriple(base, left, right);

        Syncable result = defaultFieldMergeStrategy.merge(sync, syncTriple);

        assertTrue(result instanceof SyncObject);
        assertNotEquals(right, result);
        assertNotEquals(left, result);
        assertEquals(expected, result);
        verify(sync).cloneSyncable(base);
    }
}