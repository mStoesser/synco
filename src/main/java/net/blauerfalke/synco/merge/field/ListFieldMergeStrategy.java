package net.blauerfalke.synco.merge.field;

import net.blauerfalke.synco.merge.conflict.MergeConflictStrategy;
import net.blauerfalke.synco.model.Diff;
import net.blauerfalke.synco.model.SyncTriple;
import net.blauerfalke.synco.model.Syncable;
import net.blauerfalke.synco.util.SyncUtil;

import java.util.ArrayList;
import java.util.List;


public class ListFieldMergeStrategy implements FieldMergeStrategy {

    private class Tuple {
        List<Object> insert = new ArrayList<>();
        List<Object> delete = new ArrayList<>();
    }

    @Override
    public Diff<?> mergeField(Diff<?> left, Diff<?> right, SyncTriple syncTriple, MergeConflictStrategy mergeConflictStrategy) {
        if (left.to == null && right.to == null) {
            return new Diff<>(left.from, null);
        }

        Class<?> type = SyncUtil.findType(left.from, left.to, right.from, right.to);
        if (!List.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("wrong type");
        }

        Tuple changedLeft = calculateChanges((List<?>) left.from, (List<?>) left.to);
        Tuple changesRight = calculateChanges((List<?>) right.from, (List<?>) right.to);

        if (left.to == null) {
            if (changesRight.insert.isEmpty() && changesRight.delete.isEmpty()) {
                return new Diff<>(left.from, left.to);
            } else {
                return mergeConflictStrategy.mergeField(left, right, syncTriple);
            }
        }

        if (right.to == null) {
            if (changedLeft.insert.isEmpty() && changedLeft.delete.isEmpty()) {
                return new Diff<>(left.from, right.to);
            } else {
                return mergeConflictStrategy.mergeField(left, right, syncTriple);
            }
        }

        Tuple changes = merge(changedLeft, changesRight, syncTriple, mergeConflictStrategy);

        List<Object> merge = new ArrayList<>((List<?>) left.from);
        merge.removeAll(changes.delete);
        merge.addAll(changes.insert);

        return new Diff<>(left.from, merge);
    }

    private Tuple calculateChanges(List<?> base, List<?> list) {
        Tuple tuple = new Tuple();

        if (base == null) {
            if (list != null) {
                tuple.insert.addAll(list);
            }
            return tuple;
        }

        if (list == null) {
            tuple.delete.addAll(base);
            return tuple;
        }

        for (Object o : list) {
            // if o is multiple times

            int count = count(o, list) - count(o, tuple.insert);
            if (!contains(base, o, count)) {
                tuple.insert.add(o);
            }
        }

        for (Object o : base) {
            if (!contains(list, o, count(o, base) - count(o, tuple.delete))) {
                tuple.delete.add(o);
            }
        }
        return tuple;
    }

    private Tuple merge(Tuple left, Tuple right, SyncTriple syncTriple, MergeConflictStrategy mergeConflictStrategy) {
        final Tuple changes = new Tuple();


        for (Object o : left.insert) {
            if (contains(right.delete, o)) {
                // conflict: inserted left, removed right
                Diff merged = mergeConflictStrategy.mergeField(new Diff<>(null, o), new Diff<>(o, null), syncTriple);
                if (merged.to != null) {
                    changes.insert.add(o);
                }
            } else {
                changes.insert.add(o);
            }
        }

        for (Object o : right.insert) {
            int count = count(o, right.insert) - count(o, changes.insert);
            if (contains(changes.insert, o, count)) {
                continue;
            } else if (contains(left.delete, o)) {
                // conflict: inerted right removed left
                Diff merged = mergeConflictStrategy.mergeField(new Diff<>(o, null), new Diff<>(null, o), syncTriple);
                if (merged.to != null) {
                    changes.insert.add(o);
                }
            } else {
                changes.insert.add(o);
            }
        }

        for (Object o : left.delete) {
            if (contains(right.insert, o)) {
                //conflict: inserted right, removed left
                Diff merged = mergeConflictStrategy.mergeField(new Diff<>(o, null), new Diff<>(null, o), syncTriple);
                if (merged.to == null) {
                    changes.delete.add(o);
                }
            } else {
                changes.delete.add(o);
            }
        }

        for (Object o : right.delete) {
            int count = count(o, right.delete) - count(o, changes.delete);
            if (contains(changes.delete, o, count)) {
                continue;
            } else if (contains(left.insert, o)) {
                //conflict: inserted left, removed right
                Diff merged = mergeConflictStrategy.mergeField(new Diff<>(null, o), new Diff<>(o, null), syncTriple);
                if (merged.to == null) {
                    changes.delete.add(o);
                }
            } else {
                changes.delete.add(o);
            }
        }
        return changes;
    }

//    private static String getId(Object o) {
//        if(o instanceof Syncable) {
//            ((Syncable)o).getId();
//        }
//        return o.toString();
//    }

    private static int count(Object object, final List<?>... lists) {
        if (object instanceof Syncable) {
            int n = 0;
            final String id = ((Syncable) object).getId();
            for (List<?> list : lists)
                for (Object o : list) {
                    if (o instanceof Syncable && SyncUtil.equals(id, ((Syncable) o).getId())) {
                        n++;
                    }
                }
            return n;
        } else {
            int n = 0;
            for (List<?> list : lists)
                for (Object o : list) {
                    if (SyncUtil.equals(o, object)) {
                        n++;
                    }
                }
            return n;
        }
    }

    private static boolean contains(final List<?> list, final Object object) {
        return contains(list, object, 1);
    }

    private static boolean contains(final List<?> list, final Object object, final int nTimes) {
        if (object instanceof Syncable) {
            int n = 0;
            final String id = ((Syncable) object).getId();
            for (Object o : list) {
                if (o instanceof Syncable && SyncUtil.equals(id, ((Syncable) o).getId())) {
                    if (++n >= nTimes)
                        return true;
                }
            }
        } else {
            int n = 0;
            for (Object o : list) {
                if (SyncUtil.equals(o, object)) {
                    if (++n >= nTimes)
                        return true;
                }
            }
        }
        return false;
    }
}
