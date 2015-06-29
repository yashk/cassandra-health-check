package com.lookout.cassandra;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Set;

/**
 * Created by rkuris on 6/19/15.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HealthCheckTest {
    private static final CassandraHealthCheck sut = new CassandraHealthCheck();

    @BeforeClass
    public static void setUp() throws Exception {
        // create the embedded cassandra instance and connect to it
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        sut.host = "localhost";
        sut.port = 9142;
        sut.connect();
    }

    @Test
    public void testAHostCount() throws Exception {
        Assert.assertEquals(1, sut.hostCount());
    }

    @Test
    public void testBKeyspaceDoesNotExist() throws Exception {
        Assert.assertFalse(sut.healthCheckKeyspaceExists());
    }

    @Test
    public void testCCreateKeyspace() throws Exception {
        sut.createKeyspace(1);
        Assert.assertTrue(sut.healthCheckKeyspaceExists());
        Assert.assertEquals(1, sut.getHealthcheckKeyspaceReplicationFactor());
    }

    @Test
    public void testDHealthCheck() throws Exception {
        Assert.assertEquals(0, sut.healthCheck());
    }

    @Test
    public void testEFailingOneNode() throws Exception {
       sut.dropKeyspace();
       sut.createKeyspace(2);
       Assert.assertEquals(1, sut.healthCheck());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testFUnresponsiveCoordinator() {
        sut.dropKeyspace();
        sut.createKeyspace(1);

        // suspend all the SharedPool threads, which are running cassandra
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getName().startsWith("SharedPool")) {
                thread.suspend();	// unfortunately, deprecated, hence the suppressed warning above
            }
        }
        // this should time out and fail the health check with an ERROR
        Assert.assertEquals(2, sut.healthCheck());

        // resume those threads, otherwise things get ugly later
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getName().startsWith("SharedPool")) {
                thread.resume();
            }
        }
    }

    @Test
    public void testGDropKeyspace() throws Exception {
        sut.dropKeyspace();
        Assert.assertFalse(sut.healthCheckKeyspaceExists());
    }

    @Test(expected = java.lang.IllegalStateException.class)
    public void testHMultipleConnect() {
        sut.connect();
    }

    @Test
    public void testIShutdown() throws Exception {
        sut.shutdown();
    }
}
