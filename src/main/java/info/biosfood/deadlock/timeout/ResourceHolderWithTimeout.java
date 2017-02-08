package info.biosfood.deadlock.timeout;

import info.biosfood.deadlock.resources.Resource1;
import info.biosfood.deadlock.resources.Resource2;
import info.biosfood.deadlock.resources.Resource3;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

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
