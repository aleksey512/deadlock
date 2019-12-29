# Deadlock. When a deadlock happens and how to avoid it

## Introduction
*Deadlock* is a situation when two or more threads are sharing the same resources, preventing each other to access a resource, 
holding other resources and waiting for each other to release the required resources. The situation causes halting further work of software.

### Example of Deadlock
I have three threads and the threads have following orders of resources acquisition:

`Main thread → Resource 1 → Resource 2 → Resource 3`

`Thread 1 → Resource 3 → Resource 2 → Resource 1`

`Thread 2 → Resource 2 → Resource 1 → Resource 3`

The resources are acquired not in the same order, that is a pure condition of Deadlock appearance.

| Main | Thread1 | Thread2 |
|------|---------|---------|
|Acquired Resource 1| | |
| |Acquired Resource 3| |
| | |Acquired Resource 2|
| | Waiting for Resource 2 Acquired by Thread2, not release | |
| Waiting for Resource 2 Acquired by Thread2, not released |   | |
|  |  | Waiting for Resource 1 Acquired by Thread2, not released |

In a grid above I wrote one of many scenarios of resources acquisition by three threads. In this particular case all 
threads are locked and waiting for each other to acquire other resources to accomplish their work. Main thread acquired 
Resource 1, Thread1 acquired Resource 3, Thread2 acquired Resource 2. Thread2 wants to acquire Resource 1, which is locked 
by Main thread and Main thread wants to acquire Resource 2, which is locked by Thread2 - deadlock.
 Two threads waiting for each other and can't go on.

```java
public class ResourceHolderDeadLockTest {

    public static final Logger LOG = Logger.getLogger(ResourceHolderDeadLockTest.class);
    
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
                LOG.debug("acquiring resource 2");
                    synchronized (resourceHolder.getResource2()) {
                    LOG.debug("acquiring resource 1");
                        synchronized (resourceHolder.getResource1()) {
                        
                        }
                    }
                }
        };
    }

    Runnable getThread2() {
        return () -> {
            LOG.debug("acquiring resource 2");
            synchronized (resourceHolder.getResource2()) {
                LOG.debug("acquiring resource 1");
                synchronized (resourceHolder.getResource1()) {
                    LOG.debug("acquiring resource 3");
                    synchronized (resourceHolder.getResource3()) {
                    }
                }
            }
        };
    }

}
```
## Put in order the resources acquisition
One way to avoid the Deadlock when a set of resources is shared simultaneously is work out a single order of resources 
acquisition. The sequence could be started in any place for acquiring the resources, but the main rule is acquire 
the resources in the same order every time.
For example your have four shared resources: Resource 1, Resource 2, Resource 3, Resource 4. If all threads follow the 
acquisition order `Resource 1 → Resource 2 → Resource 3 → Resource 4` then no deadlock will appear. 
The acquisition could be started from first, second etc. resource, but the direction have to be the same every time.

In a test below, there are three threads, which share three resource. The same resources as in the example before, 
but now all threads have the same order of acquisition.

`Main thread: Resource 1 → Resource 2 → Resource 3`

`Thread 1: Resource 3`

`Thread 2: Resource 2 → Resource 3`

In this case the Deadlock will not appear.

```java
public class ResourceHolderOrderedAcquisitionTest {

    public static final Logger LOG = Logger.getLogger(ResourceHolderDeadLockTest.class);
    
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
```

### Timeout
Another way to avoid the Deadlock is to use timeout for acquisition. If during some time the acquisition is not successful 
then interrupt the attempt, release all acquired resources before and repeat attempt later.

In a package `java.util.concurrent.locks` there are multiple locks for us which implement general interface `Lock`. 
A lock works similar with `synchronized`, but has more features. Code can use a `tryLock` method to attempt to lock 
with a timeout. In case the attempt with `timeout` the attempt is terminated after time specified as the timeout 
if the lock has not been acquired. If the timeout allows to interrupt the attempt and signal about the acquisition problem, 
then we can release other already acquired resources if one of the required resources is not acquired - there is not point 
to hold all resources if we can't finish the job. It's better to start the job over again later. Releasing the resource 
allows other thread acquire them and perform their job.

In the following example, I have modified the `ResourceHolder` class and I have modified a test file, but left a resource 
acquisition order the same. As in the first example, threads try to acquire resource not in the same order. 
In the first example above it causes deadlock.

##### New resource holder
```java
public class ResourceHolderWithTimeout {

    private ReentrantLock resource1Lock = new ReentrantLock();
    private ReentrantLock resource2Lock = new ReentrantLock();
    private ReentrantLock resource3Lock = new ReentrantLock();
    
    private Resource1 resource1 = new Resource1();
    private Resource2 resource2 = new Resource2();
    private Resource3 resource3 = new Resource3();
    
    public Resource1 lockResource1() throws InterruptedException {
        if(resource1Lock.tryLock(1000, TimeUnit.MILLISECONDS)) {
            return resource1;
        } else {
            throw new InterruptedException();
        }
    }
    
    public void unlockResource1(Resource1 resource1) {
        if(resource1 != null) {
            resource1Lock.unlock();
        }
    }
    
    public Resource2 lockResource2() throws InterruptedException {
        if(resource2Lock.tryLock(1000, TimeUnit.MILLISECONDS)) {
            return resource2;
        } else {
            throw new InterruptedException();
        }
    }
    
    public void unlockResource2(Resource2 resource2) {
        if(resource2 != null) {
            resource2Lock.unlock();
        }
    }
    
    public Resource3 lockResource3() throws InterruptedException {
        if(resource3Lock.tryLock(1000, TimeUnit.MILLISECONDS)) {
            return resource3;
        } else {
            throw new InterruptedException();
        }
    }
    
    public void unlockResource3(Resource3 resource3) {
        if(resource3 != null) {
            resource3Lock.unlock();
        }
    }

}
```

I introduced a class `InfiniteTryRunnable` which implements `Runnable` interface. The point of the class to perform 
the attempts in cycle. Abstract method `invoke` should hold logic of resource acquisition, performing required actions, 
and releasing resources. Return value has `boolean` type. It should return `true` on the job is done properly and `false` if something is wrong.

##### New worker class
```java
public abstract class InfiniteTryRunnable implements Runnable {
    @Override
    public void run() {
        boolean canStop = false;
        
        do {
            canStop = invoke();
        } while(!canStop);        
    }
    
    abstract public boolean invoke();
}
```

Here is a new test file. Each thread implements `InfiniteTryRunnable` interface. As you see, three threads as in first 
example in different order try to acquire resources, it caused deadlock before. The example below contains again different 
order, but the deadlock will not be reached. It will take longer time for three thread to finish work, but the threads 
will not be locked forever. Take a look below on log output.

```java
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
```

Following log's output shows a "fight" for resources. Thread "main" finished his job first. `Resolved all resources` appeared 
first for "main" thread. Threads "Thread-0" and "Thread-1" were not able to accomplish acquisition few times, but finally 
they both accomplished their job. `Unable to resolved all resources` means that the thread probably was locked, 
but because of timeout for a lock's attempt it was able to release his lock and start the job over again and allowed 
other threads do their job by releasing held resources.

```text
DEBUG ResourceHolderWithTimeoutTest: [Thread-0]: acquiring resource 3
DEBUG ResourceHolderWithTimeoutTest: [main]: acquiring resource 1
DEBUG ResourceHolderWithTimeoutTest: [Thread-1]: acquiring resource 2
DEBUG ResourceHolderWithTimeoutTest: [Thread-1]: acquiring resource 1
DEBUG ResourceHolderWithTimeoutTest: [main]: acquiring resource 2
DEBUG ResourceHolderWithTimeoutTest: [Thread-0]: acquiring resource 2
DEBUG ResourceHolderWithTimeoutTest: [Thread-1]: Unable to resolved all resources
DEBUG ResourceHolderWithTimeoutTest: [Thread-1]: acquiring resource 2
DEBUG ResourceHolderWithTimeoutTest: [main]: acquiring resource 3
DEBUG ResourceHolderWithTimeoutTest: [Thread-0]: Unable to resolved all resources
DEBUG ResourceHolderWithTimeoutTest: [Thread-0]: acquiring resource 3
DEBUG ResourceHolderWithTimeoutTest: [main]: Resolved all resources
DEBUG ResourceHolderWithTimeoutTest: [Thread-0]: acquiring resource 2
DEBUG ResourceHolderWithTimeoutTest: [Thread-1]: acquiring resource 1
DEBUG ResourceHolderWithTimeoutTest: [Thread-1]: acquiring resource 3
DEBUG ResourceHolderWithTimeoutTest: [Thread-0]: Unable to resolved all resources
DEBUG ResourceHolderWithTimeoutTest: [Thread-0]: acquiring resource 3
DEBUG ResourceHolderWithTimeoutTest: [Thread-0]: acquiring resource 2
DEBUG ResourceHolderWithTimeoutTest: [Thread-1]: Unable to resolved all resources
DEBUG ResourceHolderWithTimeoutTest: [Thread-1]: acquiring resource 2
DEBUG ResourceHolderWithTimeoutTest: [Thread-0]: acquiring resource 1
DEBUG ResourceHolderWithTimeoutTest: [Thread-0]: Resolved all resources
DEBUG ResourceHolderWithTimeoutTest: [Thread-1]: acquiring resource 1
DEBUG ResourceHolderWithTimeoutTest: [Thread-1]: acquiring resource 3
DEBUG ResourceHolderWithTimeoutTest: [Thread-1]: Resolved all resources
```
### Conslusion
In this article I wrote about few approaches how to overcome a deadlock. In my opinion, the approach "use the same sequence 
in resources acquisition" is better, of course if you can create the sequence. Sometimes it's not possible. 
I believe performance of the the approach "use the same sequence in resources acquisition" is higher than for timeout. 
But it different application we have tricky requirements, so timeout should be forgotten.
