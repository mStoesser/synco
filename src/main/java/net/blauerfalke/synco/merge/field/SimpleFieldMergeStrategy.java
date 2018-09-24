package net.blauerfalke.synco.merge.field;


import net.blauerfalke.synco.merge.conflict.MergeConflictStrategy;
import net.blauerfalke.synco.model.Diff;
import net.blauerfalke.synco.model.SyncTriple;

public class SimpleFieldMergeStrategy implements FieldMergeStrategy {
    public Diff<?> mergeField(Diff<?> left, Diff<?> right, SyncTriple syncTriple, MergeConflictStrategy mergeConflictStrategy) {
        if((left.to != null && left.to.equals(right.to)) || (left.to == null && right.to == null)) {
            return left;
        } else {
            return mergeConflictStrategy.mergeField(left, right, syncTriple);
        }
    }
}
