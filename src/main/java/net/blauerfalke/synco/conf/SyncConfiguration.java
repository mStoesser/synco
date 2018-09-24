package net.blauerfalke.synco.conf;

import net.blauerfalke.synco.merge.MergeStrategy;
import net.blauerfalke.synco.merge.conflict.MergeConflictStrategy;
import net.blauerfalke.synco.merge.field.FieldMergeStrategy;

import java.util.HashMap;
import java.util.Map;


public class SyncConfiguration {

    Map<Class,MergeStrategy> mergeStrategyMap = new HashMap<Class, MergeStrategy>();
    Map<Class,Map<String,FieldMergeStrategy>> fieldMergeStrategyMap = new HashMap<Class, Map<String, FieldMergeStrategy>>();
    Map<Class,Map<String,MergeConflictStrategy>> conflictMergeStrategyFieldNameMap = new HashMap<Class, Map<String, MergeConflictStrategy>>();
    Map<Class,Map<Class,MergeConflictStrategy>> conflictMergeStrategyFieldTypeMap = new HashMap<Class, Map<Class, MergeConflictStrategy>>();

    public MergeStrategy findMergeStrategyForType(Class<?> type) {
        if(mergeStrategyMap.containsKey(type))
            return mergeStrategyMap.get(type);
        return null;
    }

    public FieldMergeStrategy findFieldMergeStrategyForSyncableTypeAndFieldName(Class<?> syncableType, String fieldName) {
        if(fieldMergeStrategyMap.containsKey(syncableType)) {
            if(fieldMergeStrategyMap.get(syncableType).containsKey(fieldName)) {
                return fieldMergeStrategyMap.get(syncableType).get(fieldName);
            }
        }
        return null;
    }

    public MergeConflictStrategy findMergeConflictStrategyForSyncableTypeAndFieldName(Class<?> syncableType, String fieldName) {
        if(conflictMergeStrategyFieldNameMap.containsKey(syncableType)) {
            if(conflictMergeStrategyFieldNameMap.get(syncableType).containsKey(fieldName)) {
                return conflictMergeStrategyFieldNameMap.get(syncableType).get(fieldName);
            }
        }
        return null;
    }

    public MergeConflictStrategy findMergeConflictStrategyForSyncableTypeAndFieldType(Class<?> syncableType, Class<?> fieldType) {
        if(conflictMergeStrategyFieldTypeMap.containsKey(syncableType)) {
            if(conflictMergeStrategyFieldTypeMap.get(syncableType).containsKey(fieldType)) {
                return conflictMergeStrategyFieldTypeMap.get(syncableType).get(fieldType);
            }
        }
        return null;
    }
}
