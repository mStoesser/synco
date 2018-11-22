/*
 * Copyright 2018 Michael Stößer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@RunWith(JUnit4.class)
public class DefaultSyncTest {

    private static final String ID = "ID";

    private SyncableProvider baseProvider = mock(SyncableProvider.class, withSettings().extraInterfaces(MetadataProvider.class));
    private MetadataProvider baseMetadataProvider = (MetadataProvider) baseProvider;
    private SyncableProvider localProvider = mock(SyncableProvider.class, withSettings().extraInterfaces(MetadataProvider.class));
    private MetadataProvider localMetadataProvider = (MetadataProvider) localProvider;
    private SyncableProvider remoteProvider = mock(SyncableProvider.class, withSettings().extraInterfaces(MetadataProvider.class));
    private MetadataProvider remoteMetadataProvider = (MetadataProvider) remoteProvider;
    private SyncConfiguration syncConfiguration = spy(new SyncConfiguration());
    private DefaultSync sync = new DefaultSync(baseProvider, localProvider, remoteProvider, syncConfiguration);

    @Before
    public void setUp() {
        when(syncConfiguration.useMetadataLocal()).thenReturn(false);
        when(syncConfiguration.useMetadataRemote()).thenReturn(false);
    }

    @Test
    public void testSyncPushesLocalChangesToRemote() {
        final SyncObject base = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        final SyncObject remote = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        final SyncObject local = new SyncObject(ID, "local", 2L, 2.7d, 200L, false);
        when(localProvider.load(anyString())).thenReturn(local);
        when(baseProvider.load(anyString())).thenReturn(base);
        when(remoteProvider.load(anyString())).thenReturn(remote);
        Syncable remoteSaved = mock(Syncable.class);
        when(remoteProvider.save(any(Syncable.class))).thenReturn(remoteSaved);

        sync.syncSyncable(ID);

        ArgumentCaptor<Syncable> argumentCaptor  = ArgumentCaptor.forClass(Syncable.class);
        verify(remoteProvider).save(argumentCaptor.capture());
        SyncObject result = (SyncObject) argumentCaptor.getValue();
        assertEquals("local", result.getName());
        assertEquals(Long.valueOf(2L), result.getNumber());
        assertEquals(Double.valueOf(2.7d), result.getReal());
        assertEquals(Long.valueOf(200L), result.getUpdated());
        verify(baseProvider).save(remoteSaved);
    }

    @Test
    public void testSyncPulesRemoteChangesToLocal() {
        final SyncObject base = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        final SyncObject local = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        final SyncObject remote = new SyncObject(ID, "remote", 3L, 3.2d, 300L, false);
        when(localProvider.load(anyString())).thenReturn(local);
        when(baseProvider.load(anyString())).thenReturn(base);
        when(remoteProvider.load(anyString())).thenReturn(remote);

        sync.syncSyncable(ID);

        ArgumentCaptor<Syncable> argumentCaptor  = ArgumentCaptor.forClass(Syncable.class);
        verify(localProvider).save(argumentCaptor.capture());
        SyncObject result = (SyncObject) argumentCaptor.getValue();
        assertEquals("remote", result.getName());
        assertEquals(Long.valueOf(3L), result.getNumber());
        assertEquals(Double.valueOf(3.2d), result.getReal());
        assertEquals(Long.valueOf(300L), result.getUpdated());
        verify(baseProvider).save(result);
    }

    @Test
    public void testSyncMergesChangesOnConflict() {
        final SyncObject base = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        final SyncObject local = new SyncObject(ID, "local", 2L, 2.7d, 200L, false);
        final SyncObject remote = new SyncObject(ID, "remote", 3L, 3.2d, 300L, false);
        when(localProvider.load(anyString())).thenReturn(local);
        when(baseProvider.load(anyString())).thenReturn(base);
        when(remoteProvider.load(anyString())).thenReturn(remote);
        Syncable remoteSaved = mock(Syncable.class);
        when(remoteProvider.save(any(Syncable.class))).thenReturn(remoteSaved);

        //when(syncConfiguration.findMergeStrategyForType(any(Class.class))).thenReturn()

        sync.syncSyncable(ID);

        ArgumentCaptor<Syncable> argumentCaptor  = ArgumentCaptor.forClass(Syncable.class);
        verify(remoteProvider).save(argumentCaptor.capture());
        SyncObject result = (SyncObject) argumentCaptor.getValue();
        assertEquals("remote", result.getName());
        assertEquals(Long.valueOf(3L), result.getNumber());
        assertEquals(Double.valueOf(3.2d), result.getReal());
        assertEquals(Long.valueOf(300L), result.getUpdated());
        verify(baseProvider).save(remoteSaved);
    }

    @Test
    public void testMetadataLoadingLocalAndRemoteChanges() {
        setupMetadata(
                new SyncObject(ID, "base", 1L, 1.5d, 100L, false),
                new SyncObject(ID, "local", 2L, 2.7d, 200L, false),
                new SyncObject(ID, "remote", 3L, 3.2d, 300L, false));

        sync.syncSyncable(ID);

        verify(localProvider).load(ID);
        verify(baseProvider).load(ID);
        verify(remoteProvider).load(ID);
    }

    @Test
    public void testMetadataLoadingRemoteChanges() {
        setupMetadata(
                new SyncObject(ID, "base", 1L, 1.5d, 100L, false),
                new SyncObject(ID, "base", 1L, 1.5d, 100L, false),
                new SyncObject(ID, "remote", 3L, 3.2d, 300L, false));

        sync.syncSyncable(ID);

        verify(remoteProvider).load(ID);
    }

    @Test
    public void testMetadataLoadingLocalChanges() {
        setupMetadata(
                new SyncObject(ID, "base", 1L, 1.5d, 100L, false),
                new SyncObject(ID, "local", 2L, 2.7d, 200L, false),
                new SyncObject(ID, "base", 1L, 1.5d, 100L, false));

        sync.syncSyncable(ID);

        verify(localProvider).load(ID);
    }

    private void setupMetadata(final SyncObject base, final SyncObject local, final SyncObject remote) {
        when(syncConfiguration.useMetadataLocal()).thenReturn(true);
        when(syncConfiguration.useMetadataRemote()).thenReturn(true);
        when(localMetadataProvider.loadMetadata(anyString())).thenReturn(local);
        when(baseMetadataProvider.loadMetadata(anyString())).thenReturn(base);
        when(remoteMetadataProvider.loadMetadata(anyString())).thenReturn(remote);
        when(localProvider.load(anyString())).thenReturn(local);
        when(baseProvider.load(anyString())).thenReturn(base);
        when(remoteProvider.load(anyString())).thenReturn(remote);
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

    @Test
    public void testSyncRemoteLoadsLocalAndBase() {
        SyncObject remote = new SyncObject(ID, "remote", 1L, 1.5d, 100L, false);
        setupMetadata(
                new SyncObject(ID, "base", 1L, 1.5d, 100L, false),
                new SyncObject(ID, "local", 1L, 1.5d, 100L, false),
                remote);
        when(syncConfiguration.useMetadataLocal()).thenReturn(false);
        when(syncConfiguration.useMetadataRemote()).thenReturn(false);

        sync.syncRemote(remote);

        verify(localProvider).load(ID);
        verify(baseProvider).load(ID);
        verifyNoMoreInteractions(remoteProvider);
    }

    @Test
    public void testSyncLocalLoadsBaseAndRemote() {
        SyncObject local = new SyncObject(ID, "local", 1L, 1.5d, 100L, false);
        setupMetadata(
                new SyncObject(ID, "base", 1L, 1.5d, 100L, false),
                local,
                new SyncObject(ID, "remote", 1L, 1.5d, 100L, false));
        when(syncConfiguration.useMetadataLocal()).thenReturn(false);
        when(syncConfiguration.useMetadataRemote()).thenReturn(false);

        sync.syncLocal(local);

        verify(baseProvider).load(ID);
        verify(remoteProvider).load(ID);
    }

    @Test
    public void testPullLoadsRemoteAndSavesItToLocal() {
        SyncObject remote = new SyncObject(ID, "remote", 1L, 1.5d, 100L, false);
        when(remoteProvider.load(anyString())).thenReturn(remote);

        sync.pull(ID);

        verify(remoteProvider).load(ID);
        verify(localProvider).save(remote);
        verify(baseProvider).save(remote);
    }

    @Test
    public void testPushLoadsRemoteAndSavesItToLocal() {
        SyncObject local = new SyncObject(ID, "local", 1L, 1.5d, 100L, false);
        when(remoteProvider.save(any(Syncable.class))).thenReturn(local);

        sync.push(local);

        verify(remoteProvider).save(local);
        verify(baseProvider).save(local);
    }

    @Test
    public void testSyncLocalWithBaseLoadsRemote() {
        SyncObject local = new SyncObject(ID, "local", 1L, 1.5d, 100L, false);
        SyncObject base = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        setupMetadata(
                base,
                local,
                new SyncObject(ID, "remote", 1L, 1.5d, 100L, false));
        when(syncConfiguration.useMetadataLocal()).thenReturn(false);
        when(syncConfiguration.useMetadataRemote()).thenReturn(false);

        sync.syncLocal(base, local);

        verify(remoteProvider).load(ID);
        verifyNoMoreInteractions(localProvider);
    }

    @Test
    public void testConstructorInitiatesSyncConfiguration() {
        DefaultSync sync = new DefaultSync(mock(SyncableProvider.class), mock(SyncableProvider.class), mock(SyncableProvider.class));
        assertNotNull(sync.getSyncConfiguration());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSyncThrowsExceptionOnSyncablesAllNull() {
        sync.syncSyncable(null, null, null);
    }

    @Test
    public void testSyncCallsPullWhenBaseAndLocalAreNull() {
        SyncObject remote = new SyncObject(ID, "remote", 1L, 1.5d, 100L, false);
        when(localProvider.save(any(Syncable.class))).thenReturn(remote);

        sync.syncSyncable(null, null, remote);

        verifyPull(remote);
    }

    @Test
    public void testSyncCallsPushWhenBaseAndRemoteAreNull() {
        SyncObject local = new SyncObject(ID, "local", 1L, 1.5d, 100L, false);
        when(remoteProvider.save(any(Syncable.class))).thenReturn(local);
        when(localProvider.save(any(Syncable.class))).thenReturn(local);

        sync.syncSyncable(null, local, null);

        verifyPush(local);
    }

    @Test
    public void testSyncCallsPushWhenLocalAndRemoteAreNull() {
        SyncObject base = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        when(remoteProvider.save(any(Syncable.class))).thenReturn(base);
        when(localProvider.save(any(Syncable.class))).thenReturn(base);

        sync.syncSyncable(base, null, null);

        verifyPush(base);
    }

    @Test
    public void testSyncCallsPushWhenRemoteIsNull() {
        SyncObject base = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        SyncObject local = new SyncObject(ID, "local", 1L, 1.5d, 100L, false);
        when(remoteProvider.save(any(Syncable.class))).thenReturn(local);
        when(localProvider.save(any(Syncable.class))).thenReturn(local);

        sync.syncSyncable(base, local, null);

        verifyPush(local);
    }

    @Test
    public void testSyncCallsPullWhenLocalIsNull() {
        SyncObject base = new SyncObject(ID, "base", 1L, 1.5d, 100L, false);
        SyncObject remote = new SyncObject(ID, "remote", 1L, 1.5d, 100L, false);
        when(localProvider.save(any(Syncable.class))).thenReturn(base);

        sync.syncSyncable(base, null, remote);

        verifyPull(remote);
    }

    @Test
    public void testSyncSavesBaseWhenBaseIsNullAndLocalRemoteEquals() {
        SyncObject local = new SyncObject(ID, "local", 1L, 1.5d, 100L, false);
        SyncObject remote = new SyncObject(ID, "remote", 1L, 1.5d, 100L, false);
        when(baseProvider.save(any(Syncable.class))).thenReturn(local);

        sync.syncSyncable(null, local, remote);

        verify(baseProvider).save(local);
    }

    @Test
    public void testSyncCallsPushWhenBaseIsNullAndLocalIsNewer() {
        SyncObject local = new SyncObject(ID, "local", 1L, 1.5d, 200L, false);
        SyncObject remote = new SyncObject(ID, "remote", 1L, 1.5d, 100L, false);
        when(remoteProvider.save(any(Syncable.class))).thenReturn(local);
        when(localProvider.save(any(Syncable.class))).thenReturn(local);

        sync.syncSyncable(null, local, remote);

        verifyPush(local);
    }

    @Test
    public void testSyncCallsPullWhenBaseIsNullAndRemoteIsNewer() {
        SyncObject local = new SyncObject(ID, "local", 1L, 1.5d, 100L, false);
        SyncObject remote = new SyncObject(ID, "remote", 1L, 1.5d, 200L, false);
        when(localProvider.save(any(Syncable.class))).thenReturn(local);

        sync.syncSyncable(null, local, remote);

        verifyPull(remote);
    }

    private void verifyPush(Syncable local) {
        verify(remoteProvider).save(local);
        verify(baseProvider).save(local);
    }

    private void verifyPull(Syncable remote) {
        verify(localProvider).save(remote);
        verify(baseProvider).save(remote);
    }
}