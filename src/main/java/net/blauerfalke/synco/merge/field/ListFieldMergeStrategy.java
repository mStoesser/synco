package net.blauerfalke.synco.merge.field;

import net.blauerfalke.synco.merge.conflict.MergeConflictStrategy;
import net.blauerfalke.synco.model.Diff;
import net.blauerfalke.synco.model.SyncTriple;
import net.blauerfalke.synco.model.Syncable;
import net.blauerfalke.synco.util.SyncUtil;

import java.util.ArrayList;
import java.util.List;


public class ListFieldMergeStrategy implements FieldMergeStrategy {

    @Override
    public Diff<?> mergeField(Diff<?> left, Diff<?> right, SyncTriple syncTriple, MergeConflictStrategy mergeConflictStrategy) {
        Class<?> type = SyncUtil.findType(left.from, left.to, right.from, right.to);
        if(!type.equals(List.class)) {
            throw new IllegalArgumentException("wrong type");
        }
        List<Object> removedObject = new ArrayList<Object>();
        List<Object> insertedObject = new ArrayList<Object>();
        final List<?> baseList = (List<?>) left.from;
        List<?> leftList = (List<?>) left.to;
        List<?> rightList = (List<?>) right.to;
        if(null != leftList) {

            //leftList.stream().filter(o -> !baseList.contains(o)).collect(Collectors.toList());

            for(Object o : leftList) {
                if(!contains(baseList, getId(o))) {
                    insertedObject.add(o);
                }
            }
            for(Object o : baseList) {
                if(!contains(leftList, getId(o))) {
                    removedObject.add(o);
                }
            }
        }
        if(null != rightList) {
            for(Object o : rightList) {
                String id = getId(o);
                if(!contains(baseList, id) ) {
                    if(contains(insertedObject, id)) {
                        // object was add in left and right -> it is like left.to and right.to equals... -> nothing to do here
                    } else {
                        if (contains(removedObject, id)) {
                            //conflict (in left removed in right inserted) -> use ConflictStrategy
                            Diff merged = mergeConflictStrategy.mergeField(new Diff<>(o, null),new Diff<>(null, o), syncTriple);
                            if(merged.to != null) {
                                removedObject.remove(o);
                                insertedObject.add(o);
                            }
                        } else {
                            // unkown object insert it
                            insertedObject.add(o);
                        }
                    }

                }
            }
            for(Object o : baseList) {
                String id = getId(o);
                if(!contains(rightList, id)) {
                    if(contains(removedObject, id)) {
                        // object was removed in left and right -> it is like left.to and right.to equals... -> nothing to do here
                    } else {
                        if (contains(insertedObject, id)) {
                            //conflict (in left inserted in right removed) -> use ConflictStrategy
                            Diff merged = mergeConflictStrategy.mergeField(new Diff<>(null, o),new Diff<>(o, null), syncTriple);
                            if(merged.to == null) {
                                insertedObject.remove(o);
                                removedObject.add(o);
                            }
                        } else {
                            // left has no touch with it -> remove it
                            removedObject.add(o);
                        }
                    }
                }
            }
        }

        List<Object> merge = new ArrayList<Object>(baseList);
        for(Object o : removedObject) {
            merge.remove(o);
        }
        for(Object o : insertedObject){
            merge.add(o);
        }
        return new Diff<Object>(baseList, merge);
    }

    private static String getId(Object o) {
        if(o instanceof Syncable) {
            ((Syncable)o).getId();
        }
        return o.toString();
    }

    private static boolean contains(final List<?> list, final String id) {
        for(Object o : list) {
            if(o instanceof Syncable && id.equals(((Syncable)o).getId())) {
                return true;
            }
        }
        return false;
    }
}
