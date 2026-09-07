package blockingqueue;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Same blocking queue, same behavior, but using java.util.concurrent.locks
 * instead of the built-in synchronized/wait/notifyAll.
 *
 *   synchronized(this) { ... }   <->   lock.lock(); try { ... } finally { lock.unlock(); }
 *   this.wait()                 <->   someCondition.await()
 *   this.notifyAll()            <->   someCondition.signalAll()
 *
 * ---------------------------------------------------------------------------
 * WHY BOTHER -- WHAT'S ACTUALLY DIFFERENT
 * ---------------------------------------------------------------------------
 * 1. Explicit lock/unlock instead of an implicit monitor.
 *    With `synchronized`, the JVM auto-releases the lock for you, even if an
 *    exception is thrown. With ReentrantLock, YOU are responsible -- if you
 *    forget to unlock() (e.g. an exception skips it), the lock stays held
 *    forever and every other thread deadlocks. This is exactly why unlock()
 *    always goes in a `finally` block below -- non-negotiable.
 *
 * 2. Multiple Conditions per lock, instead of one wait set per object.
 *    With plain synchronized/wait/notifyAll, an object has exactly ONE wait
 *    set. Calling notifyAll() wakes up EVERY thread waiting on that object --
 *    producers and consumers alike -- even ones that have no chance of
 *    proceeding. Each of those threads wakes up, re-checks its while
 *    condition, finds it's still false, and goes right back to sleep. Wasted
 *    context switches ("thundering herd").
 *
 *    Here we split that one wait set into two separate Conditions carved out
 *    of the SAME lock: `fullCondition` (producers wait here) and `emptyCondition`
 *    (consumers wait here). When put() adds an item, it only needs to wake
 *    consumers -> signalAll() on emptyCondition. It has no reason to wake other
 *    producers, so it doesn't. Same the other way in get(). This is the
 *    concrete, practical reason ReentrantLock+Condition exists.
 *
 * 3. Extra capabilities synchronized doesn't have (not used below, but worth
 *    knowing): tryLock() (attempt the lock, give up instead of blocking
 *    forever), lockInterruptibly(), and a "fair" mode
 *    (`new ReentrantLock(true)`) that grants the lock to the longest-waiting
 *    thread first -- recall that plain synchronized makes NO promise about
 *    which blocked thread wins the race for the lock.
 *
 * Everything else -- the while-not-if rule, what "releases the lock and
 * parks the thread" means, one thread at a time in the critical section --
 * is identical to the synchronized version. Only the vocabulary changed.
 * ---------------------------------------------------------------------------
 */
public class BlockingQueueUsingReentrant<T> {
    private Queue<T> queue = new LinkedList<>();
    private int capacity;
    private ReentrantLock lock = new ReentrantLock(true);
    private Condition fullCondition = lock.newCondition(); // producers wait here
    private Condition emptyCondition = lock.newCondition(); // consumers wait here

    public BlockingQueueUsingReentrant(int capacity) {
        this.capacity = capacity;
    }

    public void put(T item) throws InterruptedException {
        lock.lock(); // explicit version of entering `synchronized(this)`
        try {
            System.out.println("[Producer] trying to put " + item + "  (queue size: " + queue.size() + ")");
            while (queue.size() == capacity)
                fullCondition.await(); // like wait(), but ONLY other producers share this wait set

            queue.add(item);
            System.out.println("[Producer] put " + item + " successfully");
            emptyCondition.signalAll(); // wake ONLY consumers -- producers aren't affected
        } finally {
            lock.unlock(); // MUST be in finally -- this is the one thing synchronized did for free
        }
    }

    public T get() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty())
                emptyCondition.await();

            T polledItem = queue.poll();
            System.out.println("                              [Consumer] took " + polledItem + "  (queue size: " + queue.size() + ")");
            fullCondition.signalAll(); // wake ONLY producers -- consumers aren't affected
            return polledItem;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }
}
