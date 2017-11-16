package easy.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ChannelQueue<E> {
    private LinkedBlockingQueue<E> _queue;
    private Semaphore _semaphore;

    public ChannelQueue() {
        this(Integer.MAX_VALUE);
    }

    public ChannelQueue(int count) {
        _semaphore = new Semaphore(0);
        _queue = new LinkedBlockingQueue<E>(count);
    }

    public void add(E e) throws Exception {
        _queue.add(e);
        _semaphore.release();
    }

    public E poll() throws InterruptedException {
        return this.poll(0, TimeUnit.NANOSECONDS);
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        if (!_semaphore.tryAcquire(timeout, unit)) {
            return null;
        }
        return _queue.poll();
    }
}
