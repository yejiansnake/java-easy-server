package easy.thread;

public class WorkerGroupConfig {
    //工作内容
    public Class<? extends WorkerGroupHandler> handlerClass;
    //工作线程数
    public int workerCount = 5;
    //管道队列容量
    public int queueCapacity = Integer.MAX_VALUE;
}