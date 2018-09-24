package net.blauerfalke.synco.merge.field;

import net.blauerfalke.synco.merge.conflict.MergeConflictStrategy;
import net.blauerfalke.synco.model.Diff;
import net.blauerfalke.synco.model.SyncTriple;

public interface FieldMergeStrategy {
    Diff<?> mergeField(Diff<?> left, Diff<?> right, SyncTriple syncTriple, MergeConflictStrategy mergeConflictStrategy);
}
