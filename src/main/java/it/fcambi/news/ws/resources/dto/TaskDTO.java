package it.fcambi.news.ws.resources.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.fcambi.news.async.TaskStatus;

import java.util.Date;

/**
 * Created by Francesco on 10/11/15.
 */
public class TaskDTO {

    protected int taskId;
    protected Date created;
    protected double progress;
    protected TaskStatus status;

    public TaskDTO(int taskId, Date created, double progress, TaskStatus status) {
        this.taskId = taskId;
        this.created = created;
        this.progress = progress;
        this.status = status;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @JsonProperty
    public boolean active() {
        return (status == TaskStatus.RUNNING);
    }
}
