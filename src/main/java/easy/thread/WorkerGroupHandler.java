package easy.thread;

public interface WorkerGroupHandler {
    void handleTask(Object taskObj);
    void handleException(Exception ex, Object taskObj);
}
