package blockingqueue;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A bare-bones blocking queue, built from scratch using wait()/notifyAll(),
 * so you can see exactly what's happening under the hood (instead of just
 * using java.util.concurrent.LinkedBlockingQueue, which hides all of this).
 *
 * put()  -> blocks the caller if the queue is FULL
 * get()  -> blocks the caller if the queue is EMPTY
 *
 * ---------------------------------------------------------------------------
 * WHAT wait() AND notifyAll() ACTUALLY ARE
 * ---------------------------------------------------------------------------
 * We never defined wait()/notifyAll() anywhere. They are inherited from
 * java.lang.Object -- EVERY object in Java has them, because every class
 * implicitly extends Object. Their real implementation is native code inside
 * the JVM itself, not Java code.
 *
 * Every object carries two invisible things:
 *   1. An intrinsic lock ("monitor") -- what `synchronized` acquires/releases.
 *   2. A wait set -- a "waiting room" of threads that called wait() on it.
 *
 * Both methods only work on an object whose lock the CURRENT thread already
 * holds (i.e. called from inside a `synchronized` block/method on that same
 * object). Calling them without holding the lock throws
 * IllegalMonitorStateException. That's why every wait()/notifyAll() call
 * below lives inside a `synchronized` method.
 *
 * ---------------------------------------------------------------------------
 * WHAT HAPPENS TO A THREAD WHEN IT CALLS wait()
 * ---------------------------------------------------------------------------
 * Atomically (as one indivisible step), the JVM:
 *   a) releases the lock this thread was holding on the object
 *   b) parks the thread in the object's wait set
 *   c) suspends it -- Thread.getState() reports WAITING
 *
 * A WAITING thread uses ZERO cpu. It still exists (it's not destroyed and
 * can't be "reused" for other work) -- it just sits frozen at that exact
 * line until someone calls notify()/notifyAll() on the SAME object.
 *
 * Because the lock gets released in step (a), OTHER threads can now enter
 * this same synchronized method. If they also find the condition true, they
 * call wait() too and join the SAME wait set -- any number of threads can
 * pile up here together, all WAITING, all holding no lock, all costing
 * nothing.
 *
 * ---------------------------------------------------------------------------
 * WHAT notifyAll() ACTUALLY DOES
 * ---------------------------------------------------------------------------
 * It moves every thread OUT of the wait set into a DIFFERENT state: BLOCKED
 * (meaning: runnable, but waiting to acquire a lock someone else currently
 * holds). notifyAll() does NOT release the lock itself and does NOT
 * immediately run any woken thread -- that only happens once the notifying
 * thread's synchronized method actually returns. Then exactly one of the
 * (possibly several) BLOCKED threads wins the race for the lock, resumes
 * running from right after its own wait() call, and re-checks its while
 * condition.
 *
 * (We use notifyAll(), not notify(), because notify() wakes only ONE
 * arbitrary waiting thread -- fine with a single producer/consumer, but
 * unsafe in general since you can't control which thread it picks.)
 *
 * ---------------------------------------------------------------------------
 * WHY "while" AND NOT "if" -- THIS IS THE BUG WE FOUND AND FIXED
 * ---------------------------------------------------------------------------
 * When a woken thread resumes, it MUST re-check the condition before
 * proceeding -- because by the time it actually reacquires the lock,
 * someone else may have already changed the state (e.g. another producer
 * grabbed the freed slot first). Using `if` instead of `while` skips that
 * re-check and lets the thread barrel ahead on a stale assumption.
 *
 * With a single producer + single consumer this bug is invisible, because
 * there's only ever one thread that could be waiting on each side. It
 * becomes very real (and was reproduced) with 2+ producers: notifyAll()
 * wakes ALL of them, but only one should actually get the freed slot -- the
 * rest need to re-check, see the queue is full again, and go back to
 * wait(). `if` skips that check and lets them all add anyway, silently
 * blowing past capacity. `while` fixes it completely.
 * ---------------------------------------------------------------------------
 */
class BlockingQueueUsingSynchronization<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;

    public BlockingQueueUsingSynchronization(int capacity) {
        this.capacity = capacity;
    }

    // Producer calls this
    public synchronized void put(T item) throws InterruptedException {
        System.out.println("[Producer] trying to put " + item + "  (queue size: " + queue.size() + ")");
        while (queue.size() == capacity) {
            wait(); // release lock, park here (WAITING) until a get() frees a slot and notifies us
        }
        queue.add(item);
        System.out.println("[Producer] put " + item + " successfully");
        notifyAll(); // wake up anyone (consumers) parked waiting for an item to appear
    }

    // Consumer calls this
    public synchronized T get() throws InterruptedException {
        while (queue.isEmpty()) {
            wait(); // release lock, park here (WAITING) until a put() adds an item and notifies us
        }
        T item = queue.poll();
        System.out.println("                              [Consumer] took " + item + "  (queue size: " + queue.size() + ")");
        notifyAll(); // wake up anyone (producers) parked waiting for a free slot
        return item;
    }

    // synchronized so a caller always sees a size that's consistent with a
    // real instant in time -- note this can still go "stale" the moment
    // another thread acts right after this call returns; that's normal and
    // expected in concurrent code, not a bug.
    public synchronized int size() {
        return queue.size();
    }
}