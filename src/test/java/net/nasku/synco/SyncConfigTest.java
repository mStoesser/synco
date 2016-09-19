package net.nasku.synco;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Created by Seven on 15.09.2016.
 */
public class SyncConfigTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testLoadConfig() throws Exception {

        Properties properties = new Properties();
        properties.setProperty("key", "value");
        properties.setProperty("some.recursive.key", "recursiveValue");

        SyncConfig syncConfig = SyncConfig.loadConfig(properties);
        assertEquals("value", syncConfig.getString("key", null));
        assertEquals("default", syncConfig.getString("notSet", "default"));

        Object some = syncConfig.values.get("some");
        assertTrue(some instanceof Map);
        Map<String,Object> map = (Map<String, Object>) some;
        Object recursive = map.get("recursive");
        assertTrue(some instanceof Map);
        map = (Map<String, Object>) recursive;
        assertTrue(map.containsKey("key"));
        assertEquals("recursiveValue", map.get("key"));
    }

    @Test
    public void testPut() throws Exception {
        SyncConfig syncConfig = new SyncConfig();
        assertEquals(0, syncConfig.values.size());
        syncConfig.put("key", "value");
        assertEquals(1, syncConfig.values.size());
        assertTrue(syncConfig.values.containsKey("key"));
        assertEquals("value", syncConfig.values.get("key"));

        // Well not sure if put should be automatically add an recursive structure
        syncConfig.put("a.b", "c");
        assertTrue(syncConfig.values.containsKey("a.b"));
        assertEquals("c", syncConfig.values.get("a.b"));
//        Map<String,Object> map = syncConfig.getMap("a");
//        assertNotNull(map);
//        assertEquals(1, map.size());
//        assertEquals("c", map.get("b"));
    }

    @Test
    public void testKeySet() throws Exception {
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.keySet();
        assertEquals(syncConfig.values.keySet(), syncConfig.keySet());
    }

    @Test
    public void testGetString() throws Exception {
        SyncConfig syncConfig = new SyncConfig();
        assertEquals("default", syncConfig.getString("not-existing-key", "default"));
        syncConfig.put("key", "value");
        assertEquals("value", syncConfig.getString("key", null));
        syncConfig.put("int", 42);
        assertEquals("42", syncConfig.getString("int", null));
        syncConfig.put("long", 42l);
        assertEquals("42", syncConfig.getString("long", null));
        syncConfig.put("float", 42.23f);
        assertEquals("42.23", syncConfig.getString("float", null));
        syncConfig.put("double", 42.23d);
        assertEquals("42.23", syncConfig.getString("double", null));
        syncConfig.put("boolean", true);
        assertEquals("true", syncConfig.getString("boolean", null));
    }

    @Test
    public void testGetFloat() throws Exception {
        SyncConfig syncConfig = new SyncConfig();
        assertNull(syncConfig.getFloat("not-existing-key", null));
        assertEquals(42.23f, syncConfig.getFloat("not-existing-key", 42.23f), 0f);
        syncConfig.put("float", 23.42f);
        assertEquals(23.42f, syncConfig.getFloat("float", null), 0f);
        syncConfig.put("str", "42.23");
        assertEquals(42.23f, syncConfig.getFloat("str", null), 0f);
        syncConfig.put("int", 23);
        assertEquals(23f, syncConfig.getFloat("int", null), 0f);
        syncConfig.put("long", 23l);
        assertEquals(23f, syncConfig.getFloat("long", null), 0f);
        syncConfig.put("double", 23.42d);
        assertEquals(23.42f, syncConfig.getFloat("double", null), 0f);
        syncConfig.put("boolean", true);
        assertNull(syncConfig.getFloat("boolean", null));
    }

    @Test
    public void testGetDouble() throws Exception {
        SyncConfig syncConfig = new SyncConfig();
        assertNull(syncConfig.getDouble("not-existing-key", null));
        assertEquals(42.23d, syncConfig.getDouble("not-existing-key", 42.23d), 0d);
        syncConfig.put("double", 23.42d);
        assertEquals(23.42d, syncConfig.getDouble("double", null), 0d);
        syncConfig.put("str", "42.23");
        assertEquals(42.23d, syncConfig.getDouble("str", null), 0d);
        syncConfig.put("int", 23);
        assertEquals(23d, syncConfig.getDouble("int", null), 0d);
        syncConfig.put("long", 23l);
        assertEquals(23d, syncConfig.getDouble("long", null), 0d);
        syncConfig.put("float", 23.42f);
        assertEquals(23.42d, syncConfig.getDouble("float", null), 0.0005d);
        syncConfig.put("boolean", true);
        assertNull(syncConfig.getDouble("boolean", null));
    }

    @Test
    public void testGetLong() throws Exception {
        SyncConfig syncConfig = new SyncConfig();
        assertNull(syncConfig.getLong("not-existing-key", null));
        assertEquals(42l, (long)syncConfig.getLong("not-existing-key", 42l));
        syncConfig.put("long", 23l);
        assertEquals(23l, (long)syncConfig.getLong("long", null));
        syncConfig.put("double", 23d);
        assertEquals(23l, (long)syncConfig.getLong("double", null));
        syncConfig.put("str", "42");
        assertEquals(42l, (long)syncConfig.getLong("str", null));
        syncConfig.put("int", 23);
        assertEquals(23l, (long)syncConfig.getLong("int", null));
        syncConfig.put("float", 23f);
        assertEquals(23l, (long)syncConfig.getLong("float", null));
        syncConfig.put("boolean", true);
        assertNull(syncConfig.getLong("boolean", null));
    }

    @Test
    public void testGetInteger() throws Exception {
        SyncConfig syncConfig = new SyncConfig();
        assertNull(syncConfig.getInteger("not-existing-key", null));
        assertEquals(42, (int)syncConfig.getInteger("not-existing-key", 42));
        syncConfig.put("int", 23);
        assertEquals(23, (int)syncConfig.getInteger("int", null));
        syncConfig.put("long", 23l);
        assertEquals(23, (int)syncConfig.getInteger("long", null));
        syncConfig.put("double", 23d);
        assertEquals(23, (int)syncConfig.getInteger("double", null));
        syncConfig.put("str", "42");
        assertEquals(42, (int)syncConfig.getInteger("str", null));
        syncConfig.put("float", 23f);
        assertEquals(23, (int)syncConfig.getInteger("float", null));
        syncConfig.put("boolean", true);
        assertNull(syncConfig.getInteger("boolean", null));
    }

    @Test
    public void testGetBoolean() throws Exception {
        SyncConfig syncConfig = new SyncConfig();
        assertNull(syncConfig.getBoolean("not-existing-key", null));
        assertTrue(syncConfig.getBoolean("not-existing-key", true));
        assertFalse(syncConfig.getBoolean("not-existing-key", false));
        syncConfig.put("boolean", true);
        assertTrue(syncConfig.getBoolean("boolean", null));
        syncConfig.put("boolean", false);
        assertFalse(syncConfig.getBoolean("boolean", null));
        syncConfig.put("int", 23);
        assertFalse(syncConfig.getBoolean("int", null));
        syncConfig.put("int2", 0);
        assertTrue(syncConfig.getBoolean("int2", null));
        syncConfig.put("long", 23l);
        assertFalse(syncConfig.getBoolean("long", null));
        syncConfig.put("long2", 0l);
        assertTrue(syncConfig.getBoolean("long2", null));
        syncConfig.put("float", 23.42f);
        assertFalse(syncConfig.getBoolean("float", null));
        syncConfig.put("float2", 0f);
        assertTrue(syncConfig.getBoolean("float2", null));
        syncConfig.put("double", 23.42d);
        assertFalse(syncConfig.getBoolean("double", null));
        syncConfig.put("double2", 0d);
        assertTrue(syncConfig.getBoolean("double2", null));
        syncConfig.put("str", "true");
        assertTrue(syncConfig.getBoolean("str", null));
        syncConfig.put("str", "True");
        assertTrue(syncConfig.getBoolean("str", null));
        syncConfig.put("str", "tRuE");
        assertTrue(syncConfig.getBoolean("str", null));
        syncConfig.put("str", " true  ");
        assertTrue(syncConfig.getBoolean("str", null));
        syncConfig.put("str", "false");
        assertFalse(syncConfig.getBoolean("str", null));
        syncConfig.put("str", "False");
        assertFalse(syncConfig.getBoolean("str", null));
        syncConfig.put("str", "fAlSe");
        assertFalse(syncConfig.getBoolean("str", null));
        syncConfig.put("str", " false  ");
        assertFalse(syncConfig.getBoolean("str", null));
        syncConfig.put("str", "dfkjsdf");
        assertNull(syncConfig.getBoolean("str", null));
    }

    @Test
    public void testGetMap() throws Exception {
        SyncConfig syncConfig = new SyncConfig();
        Map map = syncConfig.getMap("not-existing-key");
        assertEquals(0, map.size());
        assertNull(syncConfig.getMap("not-existing-key", null));
        map = new HashMap();
        assertEquals(map, syncConfig.getMap("not-existing-key", map));
        map = new HashMap();
        syncConfig.put("map", map);
        assertEquals(map, syncConfig.getMap("map", null));
        syncConfig.put("str", "str");
        assertNull(syncConfig.getMap("str", null));
        syncConfig.put("int", 42);
        assertNull(syncConfig.getMap("int", null));
        syncConfig.put("long", 23l);
        assertNull(syncConfig.getMap("long", null));
        syncConfig.put("float", 42.23f);
        assertNull(syncConfig.getMap("float", null));
        syncConfig.put("double", 23.42d);
        assertNull(syncConfig.getMap("double", null));
        syncConfig.put("boolean", true);
        assertNull(syncConfig.getMap("boolean", null));
    }

    @Test
    public void testGetSyncConfig() throws Exception {
        SyncConfig syncConfig = new SyncConfig();
        SyncConfig sub = syncConfig.getSyncConfig("not-existing-key");
        assertEquals(0, sub.values.size());

        Map<String,Object> map = new HashMap<>();
        map.put("key", "value");
        syncConfig.put("sub", map);
        sub = syncConfig.getSyncConfig("sub");
        assertEquals(1, sub.values.size());
        assertEquals("value", sub.getString("key", null));

        syncConfig = new SyncConfig();
        Map<String,Object> common = new HashMap<>();
        common.put("url", "http://www.nasku.net/");
        common.put("over", "default");
        Map<String,Object> endpoint = new HashMap<>();
        endpoint.put("key1", "value1");
        endpoint.put("key2", "value2");
        common.put("endpoint", endpoint);
        syncConfig.put("common", common);

        Map<String, Object> sync1 = new HashMap<>();
        sync1.put("parent", "common");
        sync1.put("key", "value");
        sync1.put("over", "special");
        endpoint = new HashMap<>();
        endpoint.put("key2", "haha");
        endpoint.put("key3", "value3");
        sync1.put("endpoint", endpoint);
        syncConfig.put("sync1", sync1);

        sub = syncConfig.getSyncConfig("sync1");
        assertNotNull(sub);
//        assertEquals(5, sub.values.size());
        assertEquals("value", sub.getString("key", null));
        assertEquals("common", sub.getString("parent", null));
        assertEquals("special", sub.getString("over", null));
        assertEquals("http://www.nasku.net/", sub.getString("url", null));
        SyncConfig subsub = sub.getSyncConfig("endpoint");
//        assertEquals(3, subsub.values.size());
        assertEquals("value1", subsub.getString("key1", null));
        assertEquals("haha", subsub.getString("key2", null));
        assertEquals("value3", subsub.getString("key3", null));

    }
}