package it.fcambi.news.model.tasks;

import it.fcambi.news.async.Task;
import it.fcambi.news.async.TaskStatus;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by Francesco on 04/11/15.
 */
@Entity
public class PastTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;
    protected String name;
    protected String description;
    protected Date creationTime;
    protected Date startTime;
    protected Date endTime;
    protected String result;
    @Column(columnDefinition = "TEXT")
    protected String detailedResult;

    public static PastTask createFrom(Task t) {
        PastTask p = new PastTask();
        p.name = t.getName();
        p.description = "";
        if (t.hasScheduleTime() && !t.isPeriodic())
            p.description = String.format("Scheduled @ %1$Tc;", t.getScheduleTime());
        else if (t.isPeriodic())
            p.description = String.format("Periodic task starting @ %1$Tc repeating every %2$d milliseconds;", t.getScheduleTime(), t.getPeriod());
        p.description += String.format("Task created by %1$s", t.getCreator());
        p.creationTime = t.getCreationTime();
        p.startTime = t.getStartTime();
        p.endTime = t.getEndTime();
        if (t.getStatus() == TaskStatus.COMPLETED)
            p.result = "COMPLETED";
        else if (t.getStatus() == TaskStatus.ERROR) {
            p.result = "FAILED";
            p.detailedResult = String.format("Exception at %1$.4f %% because %2$s%n%3$s",
                    t.getProgress(), t.getException().toString(), Arrays.toString(t.getException().getStackTrace()));
        } else
            throw new IllegalArgumentException("Task must be finished to create PastTask object");

        return p;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getDetailedResult() {
        return detailedResult;
    }

    public void setDetailedResult(String detailedResult) {
        this.detailedResult = detailedResult;
    }

    @Override
    public String toString() {
        return String.format("{ id: %1$s, name: %2$s, description: %3$s, creationTime: %4$Tc," +
                " startTime: %5$Tc, endTime: %6$Tc, result: %7$s, detailedResult: %8$s }", id, name,
                description, creationTime, startTime, endTime, result, detailedResult);
    }
}
