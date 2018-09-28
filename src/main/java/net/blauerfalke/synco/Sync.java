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

    private static Map<Class<?>,Object> singletons = new HashMap<>();
    static {
        singletons.put(DefaultFieldMergeStrategy.class, new DefaultFieldMergeStrategy());
        singletons.put(ListFieldMergeStrategy.class, new ListFieldMergeStrategy());
        singletons.put(SimpleFieldMergeStrategy.class, new SimpleFieldMergeStrategy());
        singletons.put(TakeNewerOnMergeConflictStrategy.class, new TakeNewerOnMergeConflictStrategy());
    }

    private SyncableProvider provider;
    private SyncableProvider remoteAdapter;
    private SyncConfiguration syncConfiguration;

    public Sync(SyncableProvider provider, SyncableProvider remoteAdapter) {
        this(provider, remoteAdapter, new SyncConfiguration());
    }

    public Sync(SyncableProvider provider, SyncableProvider remoteAdapter, SyncConfiguration syncConfiguration) {
        this.provider = provider;
        this.remoteAdapter = remoteAdapter;
        this.syncConfiguration = syncConfiguration;
    }

    public void syncSyncable(String id) {

        Syncable local = provider.load(id);
        Syncable base = provider.load(local.getId()+"-base");
        Syncable remote = remoteAdapter.load(local.getId());

        if (local.getUpdated().equals(base.getUpdated())) {
            // no local changes -> check for remote changes

            if (base.getUpdated().equals(remote.getUpdated())) {
                // no local changes, no remote -> nothing to do
                log.debug("object '%s' is in sync",id);

            } else if (base.getUpdated() < remote.getUpdated()) {
                // not local changes, remote changes -> pull
                provider.save(id, remote);
                provider.save(id+"-base", remote);
            } else {
                // base is newer than remote -> should never happen -> (but fixed with push)
                push(local);
            }

        } else if(local.getUpdated() > base.getUpdated()) {
            // local changes

            if (base.getUpdated().equals(remote.getUpdated())) {
                // local changes, no remote changes -> push
                push(local);

            } else if (base.getUpdated() < remote.getUpdated()) {
                // local changes, remote changes -> merge

                MergeStrategy mergeStrategy = getMergeStrategy(SyncUtil.findType(base, local, remote));
                Syncable merged = mergeStrategy.merge(this, new SyncTriple(base, local, remote));
                push(merged);

            } else {
                // remote is older than base -> should never happen -> (but fixed with push)
                log.error("remote-version is older than base-version from syncable: %s", id);
                push(local);
            }
        } else {
            // local is older than base -> should never happen -> (but fixed with save base-version)
            log.error("local-version is older than base-version from syncable: %s", id);
            provider.save(id+"-base", local);
        }
    }

    private void push(Syncable syncable) {
        Syncable lastRemote = remoteAdapter.save(syncable.getId(), syncable);
        provider.save(syncable.getId()+"-base", lastRemote);
    }

    private MergeStrategy getMergeStrategy(Class<?> type) {
        MergeStrategy mergeStrategy = syncConfiguration.findMergeStrategyForType(type);
        if(null != mergeStrategy)
            return mergeStrategy;
        return (MergeStrategy) singletons.get(DefaultFieldMergeStrategy.class);
    }

    public FieldMergeStrategy getFieldMergeStrategy(Class<?> syncableType, String fieldName, Class<?> fieldType) {
        FieldMergeStrategy fieldMergeStrategy = syncConfiguration.findFieldMergeStrategyForSyncableTypeAndFieldName(syncableType, fieldName);
        if(null != fieldMergeStrategy)
            return fieldMergeStrategy;

        if(fieldType.equals(List.class)) {
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
