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

import lombok.extern.slf4j.Slf4j;
import net.blauerfalke.synco.conf.SyncConfiguration;
import net.blauerfalke.synco.merge.DefaultFieldMergeStrategy;
import net.blauerfalke.synco.merge.MergeStrategy;
import net.blauerfalke.synco.merge.conflict.MergeConflictStrategy;
import net.blauerfalke.synco.merge.conflict.TakeNewerOnMergeConflictStrategy;
import net.blauerfalke.synco.merge.field.FieldMergeStrategy;
import net.blauerfalke.synco.merge.field.ListFieldMergeStrategy;
import net.blauerfalke.synco.merge.field.SimpleFieldMergeStrategy;
import net.blauerfalke.synco.model.SyncTriple;
import net.blauerfalke.synco.model.Syncable;
import net.blauerfalke.synco.util.SyncUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class Sync {

    public static final String BASE = "-base";
    private static Map<Class<?>,Object> singletons = new HashMap<>();
    static {
        singletons.put(DefaultFieldMergeStrategy.class, new DefaultFieldMergeStrategy());
        singletons.put(ListFieldMergeStrategy.class, new ListFieldMergeStrategy());
        singletons.put(SimpleFieldMergeStrategy.class, new SimpleFieldMergeStrategy());
        singletons.put(TakeNewerOnMergeConflictStrategy.class, new TakeNewerOnMergeConflictStrategy());
    }

    private SyncableProvider localProvider;
    private SyncableProvider remoteProvider;
    private SyncConfiguration syncConfiguration;

    public Sync(SyncableProvider localProvider, SyncableProvider remoteProvider) {
        this(localProvider, remoteProvider, new SyncConfiguration());
    }

    public Sync(SyncableProvider localProvider, SyncableProvider remoteProvider, SyncConfiguration syncConfiguration) {
        this.localProvider = localProvider;
        this.remoteProvider = remoteProvider;
        this.syncConfiguration = syncConfiguration;
    }



    public void syncSyncable(String id) {

        boolean baseIsFullyLoaded = false;
        boolean localIsFullyLoaded = false;
        boolean remoteIsFullyLoaded = false;
        Syncable base;
        Syncable local;
        Syncable remote;

        if(localProvider instanceof MetadataProvider && syncConfiguration.useMetadataLocal()) {
            base = ((MetadataProvider) localProvider).loadMetadata(id+BASE);
        } else {
            base = localProvider.load(id+BASE);
            baseIsFullyLoaded = true;
        }

        if(localProvider instanceof MetadataProvider && syncConfiguration.useMetadataLocal()) {
            local = ((MetadataProvider) localProvider).loadMetadata(id);
        } else {
            local = localProvider.load(id);
            localIsFullyLoaded = true;
        }

        if(remoteProvider instanceof MetadataProvider && syncConfiguration.useMetadataRemote()) {
            remote = ((MetadataProvider) remoteProvider).loadMetadata(id);
        } else {
            remote = remoteProvider.load(id);
            remoteIsFullyLoaded = true;
        }

        syncSyncable(base, local, remote, baseIsFullyLoaded, localIsFullyLoaded, remoteIsFullyLoaded);
    }

    public void syncSyncable(Syncable base, Syncable local, Syncable remote) {
        final boolean localIsFullyLoaded = !(localProvider instanceof MetadataProvider && syncConfiguration.useMetadataLocal());
        final boolean remoteIsFullyLoaded = !(remoteProvider instanceof MetadataProvider && syncConfiguration.useMetadataRemote());
        syncSyncable(base, local, remote, localIsFullyLoaded, localIsFullyLoaded, remoteIsFullyLoaded);
    }

    public void syncSyncable(Syncable base, Syncable local, Syncable remote, boolean baseIsFullyLoaded, boolean localIsFullyLoaded, boolean remoteIsFullyLoaded) {

        if (local.getUpdated().equals(base.getUpdated())) {
            // no local changes -> check for remote changes

            if (base.getUpdated().equals(remote.getUpdated())) {
                // no local changes, no remote -> nothing to do
                log.debug("object '{}' is in sync",base.getId());

            } else if (base.getUpdated() < remote.getUpdated()) {
                // not local changes, remote changes -> pull
                if(!remoteIsFullyLoaded) {
                    remote = remoteProvider.load(remote.getId());
                }
                localProvider.save(remote.getId(), remote);
                localProvider.save(remote.getId()+BASE, remote);
            } else {
                // base is newer than remote -> should never happen -> (but fixed with push)
                if(!localIsFullyLoaded) {
                    local = localProvider.load(local.getId());
                }
                push(local);
            }

        } else if(local.getUpdated() > base.getUpdated()) {
            // local changes

            if (base.getUpdated().equals(remote.getUpdated())) {
                // local changes, no remote changes -> push
                if(!localIsFullyLoaded) {
                    local = localProvider.load(local.getId());
                }
                push(local);

            } else if (base.getUpdated() < remote.getUpdated()) {
                // local changes, remote changes -> merge
                if(!localIsFullyLoaded) {
                    local = localProvider.load(local.getId());
                }
                if(!baseIsFullyLoaded) {
                    base = localProvider.load(base.getId() + BASE);
                }
                if(!remoteIsFullyLoaded) {
                    remote = remoteProvider.load(remote.getId());
                }
                MergeStrategy mergeStrategy = getMergeStrategy(SyncUtil.findType(base, local, remote));
                Syncable merged = mergeStrategy.merge(this, new SyncTriple(base, local, remote));
                push(merged);

            } else {
                // remote is older than base -> should never happen -> (but fixed with push)
                log.error("remote-version is older than base-version from syncable: '{}'", local.getId());
                if(!localIsFullyLoaded) {
                    local = localProvider.load(local.getId());
                }
                push(local);
            }
        } else {
            // local is older than base -> should never happen -> (but fixed with save base-version)
            log.error("local-version is older than base-version from syncable: '{}'", local.getId());
            if(!localIsFullyLoaded) {
                local = localProvider.load(local.getId());
            }
            localProvider.save(local.getId()+BASE, local);
        }
    }

    private void push(Syncable syncable) {
        Syncable lastRemote = remoteProvider.save(syncable.getId(), syncable);
        localProvider.save(syncable.getId()+BASE, lastRemote);
    }

    public MergeStrategy getMergeStrategy(Class<?> type) {
        MergeStrategy mergeStrategy = syncConfiguration.findMergeStrategyForType(type);
        if(null != mergeStrategy)
            return mergeStrategy;
        return (MergeStrategy) singletons.get(DefaultFieldMergeStrategy.class);
    }

    public FieldMergeStrategy getFieldMergeStrategy(Class<?> syncableType, String fieldName, Class<?> fieldType) {
        FieldMergeStrategy fieldMergeStrategy = syncConfiguration.findFieldMergeStrategyForSyncableTypeAndFieldName(syncableType, fieldName);
        if(null != fieldMergeStrategy)
            return fieldMergeStrategy;

        if (List.class.isAssignableFrom(fieldType)) {
            return (FieldMergeStrategy) singletons.get(ListFieldMergeStrategy.class);
        }
        return (FieldMergeStrategy) singletons.get(SimpleFieldMergeStrategy.class);
    }

    public MergeConflictStrategy getMergeConflictStrategy(Class<?> syncableType, String fieldName, Class<?> fieldType) {
        MergeConflictStrategy fieldMergeStrategy = syncConfiguration.findMergeConflictStrategyForSyncableTypeAndFieldName(syncableType, fieldName);
        if(null != fieldMergeStrategy)
            return fieldMergeStrategy;

        fieldMergeStrategy = syncConfiguration.findMergeConflictStrategyForSyncableTypeAndFieldType(syncableType, fieldType);
        if(null != fieldMergeStrategy)
            return fieldMergeStrategy;

        return (MergeConflictStrategy) singletons.get(TakeNewerOnMergeConflictStrategy.class);
    }



    public Syncable cloneSyncable(Syncable syncable) {
        return syncable;
    }

    public SyncConfiguration getSyncConfiguration() {
        return syncConfiguration;
    }

//
//    public static void main(String[] args) {
//        ClimbingRegion region = new ClimbingRegion();
//        region.setId("someId");
//        region.setName("aName");
//
//        System.out.println("fieldName:"+extractFieldNameFromMethodName("getBlub"));
//        System.out.println("fieldName:"+extractFieldNameFromMethodName("getblub"));
//        System.out.println("fieldName:"+extractFieldNameFromMethodName("blub"));
//
//        Map<String,Object> fields = getSyncableFields(region);
//
//        System.out.println("data:"+StringHelper.outputMap(fields));
//    }

}
