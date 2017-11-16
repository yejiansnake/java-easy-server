package easy.thread;

import easy.util.ChannelQueue;

public class WorkerGroup {

    private final static int _defaultWorkerCount = 5;
    private ChannelQueue<Object> _queue;

    public WorkerGroup() {
        this(_defaultWorkerCount);
    }

    public WorkerGroup(int count) {
        this(count, Integer.MAX_VALUE);
    }

    public WorkerGroup(int count, int capacity) {

    }
}
