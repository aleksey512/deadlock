package info.biosfood.deadlock.test;

import java.util.List;

public class ManyThreadsSimultaneously {

    private List<Runnable> jobs;

    public void setJobs(List<Runnable> jobs) {
        this.jobs = jobs;
    }

    public void execute() {
        for (Runnable job : jobs) {
            Thread t = new Thread(job);

            t.start();
        }
    }

}
