package it.fcambi.news.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Francesco on 04/11/15.
 */
public class Scheduler {

    protected ScheduledExecutorService scheduledTasksExecutor = Executors.newScheduledThreadPool(3);
    protected ExecutorService tasksExecutor = Executors.newFixedThreadPool(2);

    protected List<Task> tasks = new ArrayList<>();

    protected List<TaskCompletedObserver> observers = new ArrayList<>();

    public void schedule(Task t) {

        tasks.add(t);
        t.addCallback(this::taskCompletedCallback);
        t.queued();

        Future future;
        if (t.isPeriodic()) {
            future = scheduledTasksExecutor.scheduleAtFixedRate(t, t.getScheduleTime().getTime() - System.currentTimeMillis(),
                    t.getPeriod(), TimeUnit.MILLISECONDS);
        } else if (t.hasScheduleTime()) {
            future = scheduledTasksExecutor.schedule(t, t.getScheduleTime().getTime() - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS);
        } else {
            future = tasksExecutor.submit(t);
        }

        t.setFuture(future);

    }

    public boolean cancel(Task t) {
        if (tasks.contains(t))
            return t.getFuture().cancel(true);
        else
            return false;
    }

    public void taskCompletedCallback(Task t) {
        observers.forEach(o -> o.taskCompleted(t));
        if (!t.isPeriodic())
            tasks.remove(t);
    }

    public void addTaskCompletedObserver(TaskCompletedObserver o) {
        this.observers.add(o);
    }

    public void removeTaskCompletedObserver(TaskCompletedObserver o) {
        this.observers.remove(o);
    }

}
