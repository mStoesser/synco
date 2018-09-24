package net.blauerfalke.synco;

import net.blauerfalke.synco.model.Syncable;

public interface SyncableProvider {
    Syncable save(String id, Syncable syncable);
    Syncable load(String id);
}
