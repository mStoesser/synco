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

package net.blauerfalke.synco.conf;

import net.blauerfalke.synco.merge.MergeStrategy;
import net.blauerfalke.synco.merge.conflict.MergeConflictStrategy;
import net.blauerfalke.synco.merge.field.FieldMergeStrategy;
import net.blauerfalke.synco.model.SyncObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(JUnit4.class)
public class SyncConfigurationTest {


    @Test
    public void testMergeStrategy() {
        SyncConfiguration syncConfiguration = new SyncConfiguration();
        MergeStrategy mergeStrategy = mock(MergeStrategy.class);
        syncConfiguration.addMergeStrategyForType(SyncObject.class, mergeStrategy);

        MergeStrategy result = syncConfiguration.findMergeStrategyForType(SyncObject.class);

        assertEquals(mergeStrategy, result);
    }

    @Test
    public void testFieldMergeStrategy() {
        SyncConfiguration syncConfiguration = new SyncConfiguration();
        FieldMergeStrategy fieldMergeStrategy = mock(FieldMergeStrategy.class);
        syncConfiguration.addFieldMergeStrategy(SyncObject.class, "name", fieldMergeStrategy);

        FieldMergeStrategy result = syncConfiguration.findFieldMergeStrategyForSyncableTypeAndFieldName(SyncObject.class, "name");

        assertEquals(fieldMergeStrategy, result);
    }

    @Test
    public void testMergeConflictStrategyForFieldName() {
        SyncConfiguration syncConfiguration = new SyncConfiguration();
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);
        syncConfiguration.addMergeConflictStrategyForFieldName(SyncObject.class, "name", mergeConflictStrategy);

        MergeConflictStrategy result = syncConfiguration.findMergeConflictStrategyForSyncableTypeAndFieldName(SyncObject.class, "name");

        assertEquals(mergeConflictStrategy, result);
    }

    @Test
    public void testMergeConflictStrategyForFieldType() {
        SyncConfiguration syncConfiguration = new SyncConfiguration();
        MergeConflictStrategy mergeConflictStrategy = mock(MergeConflictStrategy.class);
        syncConfiguration.addMergeConflictStrategyForFieldType(SyncObject.class, String.class, mergeConflictStrategy);

        MergeConflictStrategy result = syncConfiguration.findMergeConflictStrategyForSyncableTypeAndFieldType(SyncObject.class, String.class);

        assertEquals(mergeConflictStrategy, result);
    }

    @Test
    public void testSyncableFields() {
        SyncConfiguration syncConfiguration = new SyncConfiguration();
        List<String> syncableFields = Arrays.asList("foo", "bar");
        syncConfiguration.addSyncableFieldsForType(SyncObject.class, syncableFields);

        List<String> result = syncConfiguration.getSyncableFieldsForType(SyncObject.class);

        assertEquals(syncableFields, result);
    }

    @Test
    public void testUseMetadataLocal() {
        SyncConfiguration syncConfiguration = new SyncConfiguration();
        assertFalse(syncConfiguration.useMetadataLocal());
        syncConfiguration.setUseMetadataLocal(true);
        assertTrue(syncConfiguration.useMetadataLocal());
        syncConfiguration.setUseMetadataLocal(false);
        assertFalse(syncConfiguration.useMetadataLocal());
    }

    @Test
    public void testUseMetadataRemote() {
        SyncConfiguration syncConfiguration = new SyncConfiguration();
        assertTrue(syncConfiguration.useMetadataRemote());
        syncConfiguration.setUseMetadataRemote(false);
        assertFalse(syncConfiguration.useMetadataRemote());
        syncConfiguration.setUseMetadataRemote(true);
        assertTrue(syncConfiguration.useMetadataRemote());
    }
}