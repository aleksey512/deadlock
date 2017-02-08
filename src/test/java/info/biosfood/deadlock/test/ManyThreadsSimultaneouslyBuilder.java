package info.biosfood.deadlock.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ManyThreadsSimultaneouslyBuilder {

    private ManyThreadsSimultaneously instance = new ManyThreadsSimultaneously();
    private ArrayList<Runnable> jobs = new ArrayList<>();

    public static ManyThreadsSimultaneouslyBuilder create() {
        return new ManyThreadsSimultaneouslyBuilder();
    }

    public ManyThreadsSimultaneouslyBuilder repeat(int times, Runnable job) {
        for(int i = 0; i < times; i++) {
            jobs.add(job);
        }

        return this;
    }

    public ManyThreadsSimultaneously build() {
        Collections.shuffle(jobs, new Random(System.nanoTime()));
        instance.setJobs(jobs);

        return instance;
    }

}
