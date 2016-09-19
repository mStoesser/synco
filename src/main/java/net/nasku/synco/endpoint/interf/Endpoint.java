package net.nasku.synco.endpoint.interf;

import net.nasku.synco.model.SyncEntity;

import java.util.List;

/**
 * Created by Seven on 23.08.2016.
 */
public interface Endpoint {
    void pull(List<SyncEntity> entities, int limit);
    void push(List<SyncEntity> entities);
}
