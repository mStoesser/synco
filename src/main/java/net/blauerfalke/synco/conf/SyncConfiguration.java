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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SyncConfiguration {

    private Map<Class,MergeStrategy> mergeStrategyMap = new HashMap<>();
    private Map<Class,Map<String,FieldMergeStrategy>> fieldMergeStrategyMap = new HashMap<>();
    private Map<Class,Map<String,MergeConflictStrategy>> conflictMergeStrategyFieldNameMap = new HashMap<>();
    private Map<Class,Map<Class,MergeConflictStrategy>> conflictMergeStrategyFieldTypeMap = new HashMap<>();
    private Map<Class,List<String>> syncableFieldsMap = new HashMap<>();

    public void addMergeStrategyForType(Class<?> type, MergeStrategy mergeStrategy) {
        mergeStrategyMap.put(type, mergeStrategy);
    }

    public MergeStrategy findMergeStrategyForType(Class<?> type) {
        if(mergeStrategyMap.containsKey(type))
            return mergeStrategyMap.get(type);
        return null;
    }

    public void addFieldMergeStrategy(Class<?> syncableType, String fieldName, FieldMergeStrategy fieldMergeStrategy) {
        if(!fieldMergeStrategyMap.containsKey(syncableType)) {
            fieldMergeStrategyMap.put(syncableType, new HashMap<>());
        }
        fieldMergeStrategyMap.get(syncableType).put(fieldName, fieldMergeStrategy);
    }

    public FieldMergeStrategy findFieldMergeStrategyForSyncableTypeAndFieldName(Class<?> syncableType, String fieldName) {
        if(fieldMergeStrategyMap.containsKey(syncableType)) {
            if(fieldMergeStrategyMap.get(syncableType).containsKey(fieldName)) {
                return fieldMergeStrategyMap.get(syncableType).get(fieldName);
            }
        }
        return null;
    }

    public void addMergeConflictStrategyForFieldName(Class<?> syncableType, String fieldName, MergeConflictStrategy mergeConflictStrategy) {
        if(!conflictMergeStrategyFieldNameMap.containsKey(syncableType)) {
            conflictMergeStrategyFieldNameMap.put(syncableType, new HashMap<>());
        }
        conflictMergeStrategyFieldNameMap.get(syncableType).put(fieldName, mergeConflictStrategy);
    }

    public MergeConflictStrategy findMergeConflictStrategyForSyncableTypeAndFieldName(Class<?> syncableType, String fieldName) {
        if(conflictMergeStrategyFieldNameMap.containsKey(syncableType)) {
            if(conflictMergeStrategyFieldNameMap.get(syncableType).containsKey(fieldName)) {
                return conflictMergeStrategyFieldNameMap.get(syncableType).get(fieldName);
            }
        }
        return null;
    }

    public void addMergeConflictStrategyForFieldType(Class<?> syncableType, Class<?> fieldType, MergeConflictStrategy mergeConflictStrategy) {
        if(!conflictMergeStrategyFieldTypeMap.containsKey(syncableType)) {
            conflictMergeStrategyFieldTypeMap.put(syncableType, new HashMap<>());
        }
        conflictMergeStrategyFieldTypeMap.get(syncableType).put(fieldType, mergeConflictStrategy);
    }

    public MergeConflictStrategy findMergeConflictStrategyForSyncableTypeAndFieldType(Class<?> syncableType, Class<?> fieldType) {
        if(conflictMergeStrategyFieldTypeMap.containsKey(syncableType)) {
            if(conflictMergeStrategyFieldTypeMap.get(syncableType).containsKey(fieldType)) {
                return conflictMergeStrategyFieldTypeMap.get(syncableType).get(fieldType);
            }
        }
        return null;
    }

    public void addSyncableFieldsForType(Class<?> type, List<String> syncableFields) {
        syncableFieldsMap.put(type, syncableFields);
    }

    public List<String> getSyncableFieldsForType(Class<?> syncableType) {
        if(syncableFieldsMap.containsKey(syncableType)) {
            return syncableFieldsMap.get(syncableType);
        }
        return Collections.emptyList();
    }
}
