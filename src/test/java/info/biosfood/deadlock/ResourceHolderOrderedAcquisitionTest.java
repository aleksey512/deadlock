package info.biosfood.deadlock;

import info.biosfood.deadlock.resources.ResourceHolder;
import info.biosfood.deadlock.test.ManyThreadsSimultaneously;
import info.biosfood.deadlock.test.ManyThreadsSimultaneouslyBuilder;
import org.apache.log4j.Logger;
import org.junit.Test;

public class ResourceHolderOrderedAcquisitionTest {

    public static final Logger LOG = Logger.getLogger(ResourceHolderOrderedAcquisitionTest.class);

    final ResourceHolder resourceHolder = new ResourceHolder();

    @Test
    public void testDeadLock() throws InterruptedException {
        ManyThreadsSimultaneously threads = ManyThreadsSimultaneouslyBuilder
                .create()
                .repeat(1, getThread1())
                .repeat(1, getThread2())
                .build();

        threads.execute();

        acquireFromMainThread();

        Thread.sleep(1000);
    }

    void acquireFromMainThread(){
        LOG.debug("acquiring resource 1");
        synchronized (resourceHolder.getResource1()) {
            LOG.debug("acquiring resource 2");
            synchronized (resourceHolder.getResource2()) {
                LOG.debug("acquiring resource 3");
                synchronized (resourceHolder.getResource3()) {
                }
            }
        }
    }

    Runnable getThread1() {
        return () -> {
            LOG.debug("acquiring resource 3");
            synchronized (resourceHolder.getResource3()) {
            }
        };
    }

    Runnable getThread2() {
        return () -> {
            LOG.debug("acquiring resource 2");
            synchronized (resourceHolder.getResource2()) {
                LOG.debug("acquiring resource 3");
                synchronized (resourceHolder.getResource3()) {
                }
            }
        };
    }

}
