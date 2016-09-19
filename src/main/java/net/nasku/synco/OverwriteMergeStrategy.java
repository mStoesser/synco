package net.nasku.synco;

import net.nasku.synco.interf.MergeStrategy;
import net.nasku.synco.model.SyncEntity;

import java.util.Map;

/**
 * Created by Seven on 12.09.2016.
 */
public class OverwriteMergeStrategy implements MergeStrategy {
    @Override
    public SyncEntity merge(SyncEntity a, SyncEntity b) {
        if(null==a)
            return null==b?null:b;
        else if(null==b)
            return a;

        for(String k : b.keySet()) {
            final Object valueA = a.get(k);
            final Object valueB = b.get(k);
            if(valueA instanceof Map && valueB instanceof Map) {
                a.put(k, SyncConfig.merge((Map)valueA, (Map)valueB));
            } else {
                a.put(k, valueB);
            }
        }
        return a;
    }
}
