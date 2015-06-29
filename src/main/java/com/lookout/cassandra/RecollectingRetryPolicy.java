package com.lookout.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.WriteType;
import com.datastax.driver.core.policies.RetryPolicy;

/**
 * This retry policy is a wrapper around another retry policy, but remembers
 * the last retry decision so the caller can check after a query is made
 */
class RecollectingRetryPolicy implements RetryPolicy {

    private final RetryPolicy policy;
    private RetryDecision decision;

    public RetryDecision getLastDecision() {
        return decision;
    }

    /**
     * Creates a new {@code RetryPolicy} that remembers if {@code policy}.downgraded or not
     *
     * @param policy the policy to wrap. The policy created by this constructor
     * will return the same decision than {@code policy} but will note the retry
     */
    public RecollectingRetryPolicy(RetryPolicy policy) {
        this.policy = policy;
    }

    @Override
    public RetryDecision onReadTimeout(Statement statement, ConsistencyLevel cl, int requiredResponses, int receivedResponses, boolean dataRetrieved, int nbRetry) {
        final RetryDecision decision = policy.onReadTimeout(statement, cl, requiredResponses, receivedResponses, dataRetrieved, nbRetry);
        this.decision = decision;
        return decision;
    }

    @Override
    public RetryDecision onWriteTimeout(Statement statement, ConsistencyLevel cl, WriteType writeType, int requiredAcks, int receivedAcks, int nbRetry) {
        final RetryDecision decision = policy.onWriteTimeout(statement, cl, writeType, requiredAcks, receivedAcks, nbRetry);
        this.decision = decision;
        return decision;

    }

    @Override
    public RetryDecision onUnavailable(Statement statement, ConsistencyLevel cl, int requiredReplica, int aliveReplica, int nbRetry) {
        RetryDecision decision = policy.onUnavailable(statement, cl, requiredReplica, aliveReplica, nbRetry);
        this.decision = decision;
        return decision;
    }
}
