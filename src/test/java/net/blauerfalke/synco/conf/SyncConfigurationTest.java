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
}