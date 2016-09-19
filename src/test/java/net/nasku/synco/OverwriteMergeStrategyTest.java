package net.nasku.synco;

import net.nasku.synco.model.SyncEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Seven on 18.09.2016.
 */
public class OverwriteMergeStrategyTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testMerge() throws Exception {

        OverwriteMergeStrategy overwriteMergeStrategy = new OverwriteMergeStrategy();
        SyncEntity a = new SyncEntity();
        SyncEntity b = new SyncEntity();

        SyncEntity c = overwriteMergeStrategy.merge(null, null);
        assertNull(c);
        
        c = overwriteMergeStrategy.merge(a, null);
        assertNotNull(c);

        c = overwriteMergeStrategy.merge(null, b);
        assertNotNull(c);

        a.put("key1", "a");
        a.put("key2", "b");
        b.put("key2", "c");
        b.put("key3", "d");
        c = overwriteMergeStrategy.merge(a, b);
        assertNotNull(c);
        assertEquals(3, a.size());
        assertEquals("a", a.get("key1"));
        assertEquals("c", a.get("key2"));
        assertEquals("d", a.get("key3"));

        a = new SyncEntity();
        b = new SyncEntity();
        Map<String,Object> map1 = new HashMap<>();
        map1.put("key1", "a");
        map1.put("key2", "b");
        Map<String,Object> map2 = new HashMap<>();
        map2.put("key2", "c");
        map2.put("key3", "d");
        a.put("map", map1);
        b.put("map", map2);
        c = overwriteMergeStrategy.merge(a, b);
        assertNotNull(c);
        assertEquals(1, c.size());
        Map map3 = (Map) c.get("map");
        assertEquals(3, map3.size());
        assertEquals("a", map3.get("key1"));
        assertEquals("c", map3.get("key2"));
        assertEquals("d", map3.get("key3"));



    }
}