package net.blauerfalke.synco;

import net.blauerfalke.synco.conf.SyncConfiguration;
import net.blauerfalke.synco.merge.DefaultFieldMergeStrategy;
import net.blauerfalke.synco.merge.MergeStrategy;
import net.blauerfalke.synco.merge.conflict.MergeConflictStrategy;
import net.blauerfalke.synco.merge.conflict.TakeNewerOnMergeConflictStrategy;
import net.blauerfalke.synco.merge.field.FieldMergeStrategy;
import net.blauerfalke.synco.merge.field.ListFieldMergeStrategy;
import net.blauerfalke.synco.merge.field.SimpleFieldMergeStrategy;
import net.blauerfalke.synco.model.SyncObject;
import net.blauerfalke.synco.model.Syncable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class SyncTest {

    private static final String ID = "ID";

    private SyncableProvider localProvider = mock(SyncableProvider.class);
    private SyncableProvider remoteProvider = mock(SyncableProvider.class);
    private SyncConfiguration syncConfiguration = spy(new SyncConfiguration());
    private Sync sync = new Sync(localProvider, remoteProvider, syncConfiguration);

    @Test
    public void testSyncPushesLocalChangesToRemote() {
        SyncObject base = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        SyncObject remote = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        SyncObject local = new SyncObject(ID, "local", 2L, 2.7d, 200L, false);
        when(localProvider.load(anyString())).thenAnswer((InvocationOnMock invocation) ->
            invocation.getArgument(0).toString().contains("-base") ? base : local
        );
        when(remoteProvider.load(anyString())).thenReturn(remote);
        Syncable remoteSaved = mock(Syncable.class);
        when(remoteProvider.save(anyString(), any(Syncable.class))).thenReturn(remoteSaved);

        sync.syncSyncable(ID);

        ArgumentCaptor<Syncable> argumentCaptor  = ArgumentCaptor.forClass(Syncable.class);
        verify(remoteProvider).save(eq(ID), argumentCaptor.capture());
        SyncObject result = (SyncObject) argumentCaptor.getValue();
        assertEquals("local", result.getName());
        assertEquals(Long.valueOf(2l), result.getNumber());
        assertEquals(Double.valueOf(2.7d), result.getReal());
        assertEquals(Long.valueOf(200l), result.getUpdated());
        verify(localProvider).save(ID+"-base", remoteSaved);
    }

    @Test
    public void testSyncPulesRemoteChangesToLocal() {
        SyncObject base = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        SyncObject local = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        SyncObject remote = new SyncObject(ID, "remote", 3L, 3.2d, 300L, false);
        when(localProvider.load(anyString())).thenAnswer((InvocationOnMock invocation) ->
                invocation.getArgument(0).toString().contains("-base") ? base : local
        );
        when(remoteProvider.load(anyString())).thenReturn(remote);

        sync.syncSyncable(ID);

        ArgumentCaptor<Syncable> argumentCaptor  = ArgumentCaptor.forClass(Syncable.class);
        verify(localProvider).save(eq(ID), argumentCaptor.capture());
        SyncObject result = (SyncObject) argumentCaptor.getValue();
        assertEquals("remote", result.getName());
        assertEquals(Long.valueOf(3l), result.getNumber());
        assertEquals(Double.valueOf(3.2d), result.getReal());
        assertEquals(Long.valueOf(300l), result.getUpdated());
        verify(localProvider).save(ID+"-base", result);
    }

    @Test
    public void testSyncMergesChangesOnConflict() {
        SyncObject base = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        SyncObject local = new SyncObject(ID, "local", 2L, 2.7d, 200L, false);
        SyncObject remote = new SyncObject(ID, "remote", 3L, 3.2d, 300L, false);
        when(localProvider.load(anyString())).thenAnswer((InvocationOnMock invocation) ->
                invocation.getArgument(0).toString().contains("-base") ? base : local
        );
        when(remoteProvider.load(anyString())).thenReturn(remote);
        Syncable remoteSaved = mock(Syncable.class);
        when(remoteProvider.save(anyString(), any(Syncable.class))).thenReturn(remoteSaved);

        //when(syncConfiguration.findMergeStrategyForType(any(Class.class))).thenReturn()

        sync.syncSyncable(ID);

        ArgumentCaptor<Syncable> argumentCaptor  = ArgumentCaptor.forClass(Syncable.class);
        verify(remoteProvider).save(eq(ID), argumentCaptor.capture());
        SyncObject result = (SyncObject) argumentCaptor.getValue();
        assertEquals("remote", result.getName());
        assertEquals(Long.valueOf(3l), result.getNumber());
        assertEquals(Double.valueOf(3.2d), result.getReal());
        assertEquals(Long.valueOf(300l), result.getUpdated());
        verify(localProvider).save(ID+"-base", remoteSaved);
    }

    @Test
    public void testGetFieldMergeStrategyReturnsListFieldMergeStrategyOnListTypes() {
        assertTrue(sync.getFieldMergeStrategy(SyncObject.class, "someFieldName", ArrayList.class) instanceof ListFieldMergeStrategy);
        assertTrue(sync.getFieldMergeStrategy(SyncObject.class, "someFieldName", Vector.class) instanceof ListFieldMergeStrategy);
        assertTrue(sync.getFieldMergeStrategy(SyncObject.class, "someFieldName", List.class) instanceof ListFieldMergeStrategy);
    }

    @Test
    public void testGetFieldMergeStrategyReturnsDefaultFieldMergeStrategyOnDefault() {
        assertTrue(sync.getFieldMergeStrategy(SyncObject.class, "someFieldName", String.class) instanceof SimpleFieldMergeStrategy);
        verify(syncConfiguration).findFieldMergeStrategyForSyncableTypeAndFieldName(SyncObject.class, "someFieldName");
    }

    @Test
    public void testGetFieldMergeStrategyCallsSyncConfigurationFindFieldMergeStrategy() {
        FieldMergeStrategy fieldMergeStrategy = mock(FieldMergeStrategy.class);
        when(syncConfiguration.findFieldMergeStrategyForSyncableTypeAndFieldName(any(Class.class), anyString())).thenReturn(fieldMergeStrategy);

        FieldMergeStrategy result = sync.getFieldMergeStrategy(SyncObject.class, "someFieldName", String.class);

        assertEquals(fieldMergeStrategy, result);
        verify(syncConfiguration).findFieldMergeStrategyForSyncableTypeAndFieldName(SyncObject.class, "someFieldName");
    }

    @Test
    public void testGetMergeStrategyReturnsDefaultMergeStrategyOnDefault() {
        assertTrue(sync.getMergeStrategy(SyncObject.class) instanceof DefaultFieldMergeStrategy);
        verify(syncConfiguration).findMergeStrategyForType(SyncObject.class);
    }

    @Test
    public void testGetMergeStrategyCallsSyncConfiguration() {
        MergeStrategy mergeStrategy = mock(MergeStrategy.class);
        when(syncConfiguration.findMergeStrategyForType(any(Class.class))).thenReturn(mergeStrategy);

        MergeStrategy result = sync.getMergeStrategy(SyncObject.class);

        assertEquals(mergeStrategy, result);
        verify(syncConfiguration).findMergeStrategyForType(SyncObject.class);
    }

    @Test
    public void testGetMergeConflictStrategyReturnsTakeNewerOnMergeConflictStrategyOnDefault() {
        assertTrue(sync.getMergeConflictStrategy(SyncObject.class, "someFieldName", String.class) instanceof TakeNewerOnMergeConflictStrategy);
        verify(syncConfiguration).findMergeConflictStrategyForSyncableTypeAndFieldName(SyncObject.class, "someFieldName");
        verify(syncConfiguration).findMergeConflictStrategyForSyncableTypeAndFieldType(SyncObject.class, String.class);
    }

    @Test
    public void testGetMergeConflictStrategyCallsSyncConfiguration() {
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);
        when(syncConfiguration.findMergeConflictStrategyForSyncableTypeAndFieldName(any(Class.class), anyString())).thenReturn(mergeConflictStrategy);

        MergeConflictStrategy result = sync.getMergeConflictStrategy(SyncObject.class, "someFieldName", String.class);

        assertEquals(mergeConflictStrategy, result);
        verify(syncConfiguration).findMergeConflictStrategyForSyncableTypeAndFieldName(SyncObject.class, "someFieldName");
        verifyNoMoreInteractions(syncConfiguration);
    }

    @Test
    public void testGetMergeConflictStrategyCallsSyncConfigurationTwice() {
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);
        when(syncConfiguration.findMergeConflictStrategyForSyncableTypeAndFieldType(any(Class.class), any(Class.class))).thenReturn(mergeConflictStrategy);

        MergeConflictStrategy result = sync.getMergeConflictStrategy(SyncObject.class, "someFieldName", String.class);

        assertEquals(mergeConflictStrategy, result);
        verify(syncConfiguration).findMergeConflictStrategyForSyncableTypeAndFieldName(SyncObject.class, "someFieldName");
        verify(syncConfiguration).findMergeConflictStrategyForSyncableTypeAndFieldType(SyncObject.class, String.class);
    }
}