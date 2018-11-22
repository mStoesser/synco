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
import net.blauerfalke.synco.merge.MergeStrategy;
import net.blauerfalke.synco.merge.conflict.MergeConflictStrategy;
import net.blauerfalke.synco.merge.field.FieldMergeStrategy;
import net.blauerfalke.synco.model.Syncable;

public interface Sync {

    void syncSyncable(String id);

    void syncRemote(Syncable remote);
    void syncRemote(Syncable remote, boolean remoteIsFullyLoaded);

    void syncLocal(Syncable local);
    void syncLocal(Syncable local, boolean localIsFullyLoaded);
    void syncLocal(Syncable base, Syncable local);
    void syncLocal(Syncable base, Syncable local, boolean baseIsFullyLoaded, boolean localIsFullyLoaded);

    void syncSyncable(Syncable base, Syncable local, Syncable remote);
    void syncSyncable(Syncable base, Syncable local, Syncable remote, boolean baseIsFullyLoaded, boolean localIsFullyLoaded, boolean remoteIsFullyLoaded);

    void push(Syncable syncable);
    Syncable pull(String id);

    SyncConfiguration getSyncConfiguration();
    MergeStrategy getMergeStrategy(Class<?> type);
    MergeConflictStrategy getMergeConflictStrategy(Class<?> syncableType, String fieldName, Class<?> fieldType);
    FieldMergeStrategy getFieldMergeStrategy(Class<?> syncableType, String fieldName, Class<?> fieldType);

    Syncable cloneSyncable(Syncable syncable);
}
