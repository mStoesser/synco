package net.blauerfalke.synco.merge.conflict;

import net.blauerfalke.synco.model.Diff;
import net.blauerfalke.synco.model.SyncTriple;

public interface MergeConflictStrategy {
    Diff<?> mergeField(Diff<?> left, Diff<?> right, SyncTriple syncTriple);
}
