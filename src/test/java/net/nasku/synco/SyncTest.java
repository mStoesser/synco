package net.nasku.synco;


import net.nasku.synco.endpoint.interf.Endpoint;
import net.nasku.synco.interf.SyncStrategy;
import net.nasku.synco.model.SyncEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;

/**
 * Created by Seven on 15.09.2016.
 */
public class SyncTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @Before
    public void setUp() throws Exception {
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @After
    public void tearDown() throws Exception {
        System.setOut(null);
        System.setErr(null);
    }

    @Test
    public void testConstruct() throws Exception {

        Sync sync = new Sync(new SyncConfig());

        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertNotNull(sync.syncStrategy);
    }
    @Test
    public void testSync() throws Exception {

        Sync sync = new Sync(new SyncConfig());
        sync.syncStrategy = new SyncStrategy() {
            @Override
            public void sync(Sync sync) {
                throw new RuntimeException("called");
            }
        };

        try {
            sync.sync();
        } catch (Exception e) {
            assert e instanceof RuntimeException;
            assert e.getMessage() == "called";
        }

    }

    @Test
    public void testSync1() throws Exception {

        Sync sync = new Sync(new SyncConfig());
        Endpoint srcEndpoint = new Endpoint() {
            @Override
            public void pull(List<SyncEntity> entities, int limit) {

            }

            @Override
            public void push(List<SyncEntity> entities) {

            }
        };
        try {
            sync.sync(srcEndpoint);
        } catch (Exception e) {}

        assertEquals(srcEndpoint, sync.srcEndpoint);
    }

    @Test
    public void testSync2() throws Exception {

        Sync sync = new Sync(new SyncConfig());
        Endpoint srcEndpoint = new Endpoint() {
            @Override
            public void pull(List<SyncEntity> entities, int limit) {

            }

            @Override
            public void push(List<SyncEntity> entities) {

            }
        };
        Endpoint destEndpoint = new Endpoint() {
            @Override
            public void pull(List<SyncEntity> entities, int limit) {

            }

            @Override
            public void push(List<SyncEntity> entities) {

            }
        };
        try {
            sync.sync(srcEndpoint, destEndpoint);
        } catch (Exception e) {}

        assertEquals(srcEndpoint, sync.srcEndpoint);
        assertEquals(destEndpoint, sync.destEndpoint);
    }

    @Test
    public void testPushDest() throws Exception {
        Sync sync = new Sync(new SyncConfig());

        // Test default-configuration
        try {
            sync.pushDest(null);
        } catch (Exception e) {
            assert e instanceof RuntimeException;
            assert e.getMessage() == "No destEnpoint found";
        }

        // Test if sync.destEndpoint is called
        final List<SyncEntity> objects = new Vector();
        Endpoint destEndpoint = new Endpoint() {
            @Override
            public void pull(List<SyncEntity> entities, int limit) {
                assertFalse("Should not called", true);
            }

            @Override
            public void push(List<SyncEntity> entities) {
                assertEquals(objects, entities);
                entities.add(new SyncEntity());
            }
        };
        sync.destEndpoint = destEndpoint;
        sync.pushDest(objects);
        assertEquals(1, objects.size());
    }

    @Test
    public void testPullDest() throws Exception {
        Sync sync = new Sync(new SyncConfig());

        // Test default-configuration
        try {
            sync.pullDest(null);
        } catch (Exception e) {
            assert e instanceof RuntimeException;
            assert e.getMessage() == "No destEnpoint found";
        }

        // Test if sync.destEndpoint is called
        final List<SyncEntity> objects = new Vector();
        Endpoint destEndpoint = new Endpoint() {
            @Override
            public void pull(List<SyncEntity> entities, int limit) {
                assertEquals(objects, entities);
                assertEquals(-1, limit);
                entities.add(new SyncEntity());
            }

            @Override
            public void push(List<SyncEntity> entities) {
                assertFalse("Should not called", true);
            }
        };
        sync.destEndpoint = destEndpoint;
        sync.pullDest(objects);
        assertEquals(1, objects.size());
    }

    @Test
    public void testPullDest1() throws Exception {
        Sync sync = new Sync(new SyncConfig());

        // Test if sync.destEndpoint is called
        final List<SyncEntity> objects = new Vector();
        Endpoint destEndpoint = new Endpoint() {
            @Override
            public void pull(List<SyncEntity> entities, int limit) {
                assertEquals(objects, entities);
                assertEquals(42, limit);
                entities.add(new SyncEntity());
            }

            @Override
            public void push(List<SyncEntity> entities) {
                assertFalse("Should not called", true);
            }
        };
        sync.destEndpoint = destEndpoint;
        sync.pullDest(objects, 42);
        assertEquals(1, objects.size());
    }

    @Test
    public void testPullSrc() throws Exception {

        Sync sync = new Sync(new SyncConfig());

        // Test default-configuration
        try {
            sync.pullSrc(null);
        } catch (Exception e) {
            assert e instanceof RuntimeException;
            assert e.getMessage() == "No srcEnpoint found";
        }

        // Test if sync.srcEndpoint is called
        final List<SyncEntity> objects = new Vector();
        Endpoint srcEndpoint = new Endpoint() {
            @Override
            public void pull(List<SyncEntity> entities, int limit) {
                assertEquals(objects, entities);
                assertEquals(-1, limit);
                entities.add(new SyncEntity());
            }

            @Override
            public void push(List<SyncEntity> entities) {
                assertFalse("Should not called", true);
            }
        };
        sync.srcEndpoint = srcEndpoint;
        sync.pullSrc(objects);
        assertEquals(1, objects.size());
    }

    @Test
    public void testPullSrc1() throws Exception {

        Sync sync = new Sync(new SyncConfig());

        final List<SyncEntity> objects = new Vector();
        Endpoint srcEndpoint = new Endpoint() {
            @Override
            public void pull(List<SyncEntity> entities, int limit) {
                assertEquals(objects, entities);
                assertEquals(23, limit);
                entities.add(new SyncEntity());
            }

            @Override
            public void push(List<SyncEntity> entities) {
                assertFalse("Should not called", true);
            }
        };
        sync.srcEndpoint = srcEndpoint;
        sync.pullSrc(objects, 23);
        assertEquals(1, objects.size());
    }

    @Test
    public void testPushSrc() throws Exception {
        Sync sync = new Sync(new SyncConfig());

        // Test default-configuration
        try {
            sync.pullSrc(null);
        } catch (Exception e) {
            assert e instanceof RuntimeException;
            assert e.getMessage() == "No srcEnpoint found";
        }

        // Test if srcEndpoint is called
        final List<SyncEntity> objects = new Vector();
        Endpoint srcEndpoint = new Endpoint() {
            @Override
            public void pull(List<SyncEntity> entities, int limit) {
                assertFalse("Should not called", true);
            }

            @Override
            public void push(List<SyncEntity> entities) {
                assertEquals(objects, entities);
                entities.add(new SyncEntity());
            }
        };
        sync.srcEndpoint = srcEndpoint;
        sync.pushSrc(objects);
        assertEquals(1, objects.size());
    }
}
