package info.biosfood.deadlock.test;

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
