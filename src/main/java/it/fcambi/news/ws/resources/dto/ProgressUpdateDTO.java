package it.fcambi.news.ws.resources.dto;

import it.fcambi.news.async.TaskStatus;

/**
 * Created by Francesco on 10/11/15.
 */
public class ProgressUpdateDTO {

    protected int taskId;
    protected TaskStatus taskStatus;
    protected double progress;

    public ProgressUpdateDTO(int taskId, TaskStatus taskStatus, double progress) {
        this.taskId = taskId;
        this.taskStatus = taskStatus;
        this.progress = progress;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }
}
