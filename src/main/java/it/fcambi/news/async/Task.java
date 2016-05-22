package it.fcambi.news.async;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Francesco on 04/11/15.
 */
public abstract class Task implements Runnable {

    private static Logger log;

    protected TaskStatus status;
    protected Progress progress = new Progress();
    protected Date creationTime;
    protected Date startTime;
    protected Date endTime;

    protected String creator;

    protected Date scheduleTime;

    protected long period = 0;

    protected List<Consumer<Task>> callbacks = new ArrayList<>();

    protected Exception exception;

    protected Future future;

    public static void setLogger(Logger l) {
        log = l;
    }

    public Task() {
        this.status = TaskStatus.NEW;
        this.creationTime = new Date();
    }

    public abstract String getName();
    public abstract String getDescription();

    @Override
    public void run() {
        this.status = TaskStatus.RUNNING;
        this.startTime = new Date();
        log.info("Starting "+getName());
        try {
            this.executeTask();
        } catch (Exception e) {
            this.exception = e;
            log.log(Level.WARNING, "Task "+getName()+" finished with exception.", e);
        }
        log.info("Completed "+getName());

        this.status = (this.exception == null) ? TaskStatus.COMPLETED : TaskStatus.ERROR;
        this.endTime = new Date();

        this.callbacks.forEach(c -> c.accept(this));
    }

    protected abstract void executeTask() throws Exception;

    public TaskStatus getStatus() {
        return status;
    }

    public double getProgress() {
        return progress.get();
    }

    public Date getStartTime() {
        if (this.startTime == null)
            throw new IllegalAccessError("Can't get starting time while task was never started");
        return startTime;
    }

    public Date getEndTime() {
        if (this.endTime == null)
            throw new IllegalAccessError("Can't get end time while task was never ended");
        return endTime;
    }

    public boolean hasScheduleTime() {
        return this.scheduleTime != null;
    }

    public boolean isPeriodic() {
        return this.period > 0;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public Date getScheduleTime() {
        if (this.scheduleTime == null)
            throw new IllegalAccessError("Can't get schedule time for non scheduled task");
        return scheduleTime;
    }

    public void setScheduleTime(Date scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void addCallback(Consumer<Task> c) {
        this.callbacks.add(c);
    }

    public void removeCallback(Consumer<Task> c) {
        this.callbacks.remove(c);
    }

    public Future getFuture() {
        if (this.status == TaskStatus.NEW)
            throw new IllegalAccessError("Can't get ScheduledFuture before scheduling task");
        return future;
    }

    public void setFuture(Future future) {
        this.future = future;
    }

    public void queued() {
        this.status = TaskStatus.QUEUED;
    }

    public String getCreator() {
        return this.creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Exception getException() {
        return exception;
    }

    public Object getResults() {
        return null;
    }
}
