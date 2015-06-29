package com.lookout.cassandra;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by rk on 6/20/15.
 */
public class CrossJVMLockTest {

    private final CrossJVMLock sut = new CrossJVMLock();

    @Test
    public void testHappyCase() throws IOException {
        sut.lock();
        sut.unlock();
    }

    @Test
    public void testNested() throws IOException {
        sut.lock();
        try {
            sut.lock();
            Assert.fail("Expected exception");
        } catch (IOException e) {
	    sut.unlock();
        }
    }

    @Test(expected = java.lang.IllegalStateException.class)
    public void testUnlockUnlocked() throws IOException {
        sut.unlock();
    }

    @Test
    public void testNewInstance() throws IOException {
        sut.lock();
        CrossJVMLock second = new CrossJVMLock();
        try {
            second.lock();
            Assert.fail("Should have thrown exception");
        } catch (IOException e) {
            Assert.assertTrue(true);
        }
        try {
            second.unlock();
            Assert.fail("Should have thrown exception");
        } catch (IllegalStateException e) {
            Assert.assertTrue(true);
        }
        sut.unlock();
    }
}
