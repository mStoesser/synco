package net.blauerfalke.synco.merge;

import net.blauerfalke.synco.Sync;
import net.blauerfalke.synco.merge.conflict.MergeConflictStrategy;
import net.blauerfalke.synco.merge.field.FieldMergeStrategy;
import net.blauerfalke.synco.model.Diff;
import net.blauerfalke.synco.model.SyncTriple;
import net.blauerfalke.synco.model.Syncable;

import java.util.Map;
import java.util.TreeMap;


public class DefaultFieldMergeStrategy implements MergeStrategy {

    @Override
    public Syncable merge(Sync sync, SyncTriple syncTriple) {

        Map<String,Diff> localChanges = Sync.calculateChanges(syncTriple.base, syncTriple.left);
        Map<String,Diff> remoteChanges = Sync.calculateChanges(syncTriple.base, syncTriple.right);

        Map<String,Diff> mergedChanges = mergeFields(sync, localChanges, remoteChanges, syncTriple);

        Syncable merged = sync.cloneSyncable(syncTriple.base);
        for(Map.Entry<String,Diff> entry : mergedChanges.entrySet()) {
            Sync.applyDiff(merged, entry.getKey(), entry.getValue());
        }
        return merged;
    }

    private Map<String,Diff> mergeFields(Sync sync, final Map<String,Diff> localChanges, final Map<String,Diff> remoteChanges, SyncTriple syncTriple) {
        final Map<String,Diff> merge = new TreeMap<String, Diff>();
        for(Map.Entry<String,Diff> entry : localChanges.entrySet()) {
            String fieldName = entry.getKey();
            Diff<?> localDiff = entry.getValue();
            if(remoteChanges.containsKey(fieldName)) {
                Diff<?> remoteDiff = remoteChanges.get(fieldName);
                Class<?> syncableType = Sync.findType(syncTriple.base, syncTriple.left, syncTriple.right);
                Class<?> fieldType = Sync.findType(localDiff.from, localDiff.to, remoteDiff.from, remoteDiff.to);
                MergeConflictStrategy mergeConflictStrategy = sync.getMergeConflictStrategy(syncableType, fieldName, fieldType);
                FieldMergeStrategy fieldMergeStrategyStrategy = sync.getFieldMergeStrategy(syncableType, fieldName, fieldType);
                Diff<?> mergedField = fieldMergeStrategyStrategy.mergeField(localDiff, remoteDiff, syncTriple, mergeConflictStrategy);
                merge.put(fieldName, mergedField);
            } else {
                merge.put(fieldName, entry.getValue());
            }
        }
        for(Map.Entry<String,Diff> entry : remoteChanges.entrySet()) {
            String fieldName = entry.getKey();
            if(!localChanges.containsKey(fieldName)) {
                merge.put(fieldName, entry.getValue());
            }
        }
        return merge;
    }
}
