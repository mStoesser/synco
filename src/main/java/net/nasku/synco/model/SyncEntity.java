package net.nasku.synco.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Seven on 23.08.2016.
 */
public class SyncEntity {

    private boolean dirty;

    public boolean isDirty() {
        return dirty;
    }

    private Map<String, Object> values = new HashMap<String,Object>();

    public Set<String> keySet() {
        return values.keySet();
    }
    public Object get(final String key) {
        return values.get(key);
    }
    public void put(String key, Object value) {
        if(values.containsKey(key) && equals(values.get(key), value))
            return;
        dirty = true;
        values.put(key, value);
    }

    public int size() {
        return values.size();
    }

    private static boolean equals(final Object a, final Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
