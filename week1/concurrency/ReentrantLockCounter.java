package concurrency;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockCounter implements Counter {

    private int counter = 0;
    private final ReentrantLock reentrantLock = new ReentrantLock();

    @Override
    public void increment() {
        reentrantLock.lock();
        try {
            counter++;
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public int get() {
        reentrantLock.lock();
        try {
            return counter;
        } finally {
            reentrantLock.unlock();
        }
    }
}
