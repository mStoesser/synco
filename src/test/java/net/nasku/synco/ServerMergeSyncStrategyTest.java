package net.nasku.synco;

import net.nasku.synco.endpoint.interf.Endpoint;
import net.nasku.synco.model.SyncEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static org.junit.Assert.*;

/**
 * Created by Seven on 19.09.2016.
 */
public class ServerMergeSyncStrategyTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testConstruct() throws Exception{
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.put("limit", 23);
        ServerMergeSyncStrategy serverMergeSyncStrategy = new ServerMergeSyncStrategy(syncConfig);
        assertEquals(23, serverMergeSyncStrategy.limit);
    }

    @Test
    public void testSync() throws Exception {

        final List<String> result = new Vector<>();
        SyncConfig syncConfig = new SyncConfig();

        Sync sync = new Sync(syncConfig);
        sync.srcEndpoint = new Endpoint() {
            @Override
            public void pull(List<SyncEntity> entities, int limit) {
                result.add("srcPull");
            }

            @Override
            public void push(List<SyncEntity> entities) {
                result.add("srcPush");
            }
        };
        sync.destEndpoint = new Endpoint() {
            @Override
            public void pull(List<SyncEntity> entities, int limit) {
                result.add("destPull");
            }

            @Override
            public void push(List<SyncEntity> entities) {
                result.add("destPush");
            }
        };

        sync.syncStrategy = new ServerMergeSyncStrategy(syncConfig);
        sync.syncStrategy.sync(sync);

        assertEquals("srcPull", result.get(0));
        assertEquals("destPush", result.get(1));
        assertEquals("srcPush", result.get(2));
        assertEquals("destPull", result.get(3));
        assertEquals("srcPush", result.get(4));
    }
}