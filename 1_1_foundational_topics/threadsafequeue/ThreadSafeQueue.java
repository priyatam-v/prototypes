package threadsafequeue;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSafeQueue<T> implements BaseQueue<T> {
    private Queue<T> queue = new LinkedList<>();
    private ReentrantLock reentrantLock = new ReentrantLock();

    @Override
    public void queue(T element) {
        reentrantLock.lock();
        try {
            queue.add(element);
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public Optional<T> deque() {
        reentrantLock.lock();
        try {
            return Optional.ofNullable(queue.poll());
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public int size() {
        reentrantLock.lock();
        try {
            return queue.size();
        } finally {
            reentrantLock.unlock();
        }
    }
}
