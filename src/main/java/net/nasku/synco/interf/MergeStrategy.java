package net.nasku.synco.interf;

import net.nasku.synco.model.SyncEntity;

/**
 * Created by Seven on 25.08.2016.
 */
public interface MergeStrategy {
    SyncEntity merge(SyncEntity a, SyncEntity b);
}
