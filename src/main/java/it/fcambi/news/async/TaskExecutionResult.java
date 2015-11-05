package it.fcambi.news.async;

/**
 * Created by Francesco on 04/11/15.
 */
public class TaskExecutionResult {

    private boolean success;
    private Exception exception;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
