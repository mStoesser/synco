package net.blauerfalke.synco.model;

import net.blauerfalke.synco.model.Syncable;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SyncTriple {
    public Syncable base;
    public Syncable left;
    public Syncable right;
}
