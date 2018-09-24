package net.blauerfalke.synco.merge.conflict;

import net.blauerfalke.synco.model.Diff;
import net.blauerfalke.synco.model.SyncTriple;
import net.blauerfalke.synco.model.Syncable;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TakeNewerOnMergeConflictStrategy implements MergeConflictStrategy {

    public Diff<?> mergeField(Diff<?> left, Diff<?> right, SyncTriple syncTriple) {
        if(syncTriple.left.getUpdated() < syncTriple.right.getUpdated())
            return right;
        return left;
    }
}
