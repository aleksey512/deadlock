package info.biosfood.deadlock;

import info.biosfood.deadlock.resources.Resource1;
import info.biosfood.deadlock.resources.Resource2;
import info.biosfood.deadlock.resources.Resource3;
import info.biosfood.deadlock.test.InfiniteTryRunnable;
import info.biosfood.deadlock.test.ManyThreadsSimultaneously;
import info.biosfood.deadlock.test.ManyThreadsSimultaneouslyBuilder;
import info.biosfood.deadlock.timeout.ResourceHolderWithTimeout;
import org.apache.log4j.Logger;
import org.junit.Test;

public class ResourceHolderWithTimeoutTest {

    public static final Logger LOG = Logger.getLogger(ResourceHolderWithTimeoutTest.class);

    final ResourceHolderWithTimeout resourceHolder = new ResourceHolderWithTimeout();

    @Test
    public void testDeadLock() throws InterruptedException {
        ManyThreadsSimultaneously threads = ManyThreadsSimultaneouslyBuilder
                .create()
                .repeat(1, getThread1())
                .repeat(1, getThread2())
                .build();

        threads.execute();

        acquireFromMainThread();

        Thread.sleep(5000);
    }

    void acquireFromMainThread() {
        new InfiniteTryRunnable() {
            public boolean invoke() {
                Resource1 r1 = null;
                Resource2 r2 = null;
                Resource3 r3 = null;

                try {
                    LOG.debug("acquiring resource 1");
                    r1 = resourceHolder.lockResource1();

                    LOG.debug("acquiring resource 2");
                    r2 = resourceHolder.lockResource2();

                    LOG.debug("acquiring resource 3");
                    r3 = resourceHolder.lockResource3();

                    LOG.debug("Resolved all resources");

                    return true;
                } catch (InterruptedException e) {
                    LOG.debug("Unable to resolved all resources");
                } finally {
                    resourceHolder.unlockResource3(r3);
                    resourceHolder.unlockResource2(r2);
                    resourceHolder.unlockResource1(r1);
                }
                return false;
            }
        }.invoke();
    }

    Runnable getThread1() {
        return new InfiniteTryRunnable() {
            public boolean invoke() {
                Resource1 r1 = null;
                Resource2 r2 = null;
                Resource3 r3 = null;

                try {
                    LOG.debug("acquiring resource 3");
                    r3 = resourceHolder.lockResource3();

                    LOG.debug("acquiring resource 2");
                    r2 = resourceHolder.lockResource2();

                    LOG.debug("acquiring resource 1");
                    r1 = resourceHolder.lockResource1();

                    LOG.debug("Resolved all resources");

                    return true;
                } catch (InterruptedException e) {
                    LOG.debug("Unable to resolved all resources");
                } finally {
                    resourceHolder.unlockResource1(r1);
                    resourceHolder.unlockResource2(r2);
                    resourceHolder.unlockResource3(r3);
                }
                return false;
            }
        };
    }

    Runnable getThread2() {
        return new InfiniteTryRunnable() {
            public boolean invoke() {
                Resource1 r1 = null;
                Resource2 r2 = null;
                Resource3 r3 = null;

                try {
                    LOG.debug("acquiring resource 2");
                    r2 = resourceHolder.lockResource2();

                    LOG.debug("acquiring resource 1");
                    r1 = resourceHolder.lockResource1();

                    LOG.debug("acquiring resource 3");
                    r3 = resourceHolder.lockResource3();
                    LOG.debug("Resolved all resources");

                    return true;
                } catch(InterruptedException e) {
                    LOG.debug("Unable to resolved all resources");
                } finally {
                    resourceHolder.unlockResource3(r3);
                    resourceHolder.unlockResource1(r1);
                    resourceHolder.unlockResource2(r2);
                }
                return false;
            }
        };
    }

}
