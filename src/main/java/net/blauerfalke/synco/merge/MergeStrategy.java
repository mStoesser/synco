package net.blauerfalke.synco.merge;

import net.blauerfalke.synco.Sync;
import net.blauerfalke.synco.model.SyncTriple;
import net.blauerfalke.synco.model.Syncable;

public interface MergeStrategy {

    Syncable merge(Sync sync, SyncTriple syncTriple);
}
