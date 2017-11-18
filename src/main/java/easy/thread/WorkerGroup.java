package easy.thread;

import easy.util.ChannelQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;

public class WorkerGroup {
    private final static int _stopWaitThreadSecond = 3;
    private WorkerGroupConfig _config;
    private boolean _close = true;
    private ChannelQueue<Object> _queue;
    private Thread[] _threads;
    private CountDownLatch _countDownLatch;

    public WorkerGroup() {
    }

    public final void run(WorkerGroupConfig config) throws Exception {
        this.checkConfig(config);

        synchronized (this) {
            if (!_close) {
                throw new Exception("instance has run");
            }
            _close = false;
        }

        _config = config;
        _queue = new ChannelQueue<Object>(_config.queueCapacity);
        this.initWorkerThreads();
    }

    public final void close() {
        synchronized (this) {
            if (_close) {
                return;
            }
            _close = true;
        }

        this.stopWorkerThreads();
        _queue = null;
        _config = null;
    }

    public final void addTask(Object task) throws Exception {
        if (task == null) {
            throw new IllegalArgumentException("config illegal");
        }

        _queue.add(task);
    }

    public final int taskCount() {
        return _queue.count();
    }

    private void checkConfig(WorkerGroupConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config illegal");
        }

        if (config.handlerClass == null) {
            throw new IllegalArgumentException("handlerClass illegal");
        }

        if (config.workerCount < 1) {
            throw new IllegalArgumentException("workerCount illegal");
        }

        if (config.queueCapacity < 1) {
            throw new IllegalArgumentException("queueCapacity illegal");
        }
    }

    private void initWorkerThreads() {
        _countDownLatch = new CountDownLatch(_config.workerCount);
        _threads = new Thread[_config.workerCount];

        for (int index = 0; index < _config.workerCount; index++) {
            _threads[index] = new Thread(new WorkerRunnable(this));
            _threads[index].start();
        }
    }

    private void stopWorkerThreads() {
        try {
            if (!_countDownLatch.await(_stopWaitThreadSecond, TimeUnit.SECONDS)) {
                for (int index = 0; index < _config.workerCount; index++) {
                    Thread thread = _threads[index];
                    if (!thread.isInterrupted()) {
                        _threads[index].interrupt();
                    }
                    _threads[index] = null;
                }
            }
        } catch (Exception ex) {
            System.out.printf("work group close exception, info:%s \n", ex.getMessage());
        }
        finally {
            _countDownLatch = null;
            _threads = null;
        }
    }

    private final Object getTask(int waitSecond) throws Exception {
        if (waitSecond < 0) {
            throw new IllegalArgumentException("waitSecond illegal");
        }
        return _queue.poll(waitSecond, TimeUnit.SECONDS);
    }

    private class WorkerRunnable implements Runnable {

        private WorkerGroup _workerGroup;

        public WorkerRunnable(WorkerGroup workerGroup) {
            _workerGroup = workerGroup;
        }

        public void run() {
            try {
                WorkerGroupHandler handler = _workerGroup._config.handlerClass.getDeclaredConstructor().newInstance();
                this.run(handler);
            } catch (Exception ex) {
                System.out.printf("work group runnable run failed, info:%s \n", ex.getMessage());
            }
        }

        public void run(WorkerGroupHandler handler) {
            Object taskObj = null;
            while (!_workerGroup._close) {
                try {
                    taskObj = _workerGroup.getTask(1);

                    if (taskObj == null) {
                        continue;
                    }

                    handler.handleTask(taskObj);

                } catch (Exception ex) {
                    handler.handleException(ex, taskObj);
                }
            }

            _workerGroup._countDownLatch.countDown();
        }
    }
}
