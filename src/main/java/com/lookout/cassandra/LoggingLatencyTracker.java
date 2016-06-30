package com.lookout.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.LatencyTracker;
import com.datastax.driver.core.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by rkuris on 6/22/15.
 */
class LoggingLatencyTracker implements LatencyTracker {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingLatencyTracker.class);

    @Override
    public void update(Host host, Statement statement, Exception exception, long newLatencyNanos) {
        if (exception == null) {
            LOG.info("Host {} latency {}ms", host, TimeUnit.NANOSECONDS.toMillis(newLatencyNanos));
        } else {
            LOG.error("Host {} threw exception {}", host, exception);
        }
    }

    @Override
    public void onRegister(Cluster cluster) {

    }

    @Override
    public void onUnregister(Cluster cluster) {

    }
}
