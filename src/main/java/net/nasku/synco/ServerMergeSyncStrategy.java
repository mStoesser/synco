package net.nasku.synco;

import net.nasku.synco.interf.SyncStrategy;
import net.nasku.synco.model.SyncEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seven on 23.08.2016.
 */
public class ServerMergeSyncStrategy implements SyncStrategy {

    int limit;

    public ServerMergeSyncStrategy(SyncConfig config) {
        this.limit = config.getInteger("limit", 10);
    }

    public void sync(Sync sync) {

        final List<SyncEntity> entities = new ArrayList<SyncEntity>(limit);

        do {
            entities.clear();
            sync.pullSrc(entities, limit);
            sync.pushDest(entities);
            sync.pushSrc(entities);
        } while(!entities.isEmpty());

        do {
            entities.clear();
            sync.pullDest(entities, limit);
            sync.pushSrc(entities);
        } while(!entities.isEmpty());

    }
}
