package threadsafequeue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

public class Demo {
    private static final int NO_OF_THREADS = Runtime.getRuntime().availableProcessors();
    private static final int ITEMS_PER_THREADS = 1000;

    public static void main(String[] args) throws InterruptedException {
        int expectedCount = NO_OF_THREADS * ITEMS_PER_THREADS;
        System.out.printf("No of threads: %d , Items per thread: %d, expected count: %d%n%n", NO_OF_THREADS, ITEMS_PER_THREADS, expectedCount);

        ThreadSafeQueue<Integer> threadSafeQueue = new ThreadSafeQueue<>();
        UnsafeQueue<Integer> unsafeQueue = new UnsafeQueue<>();

        run(unsafeQueue);
        run(threadSafeQueue);
    }

    public static void run(BaseQueue<Integer> queue) {
        try (ExecutorService threadPool = Executors.newFixedThreadPool(NO_OF_THREADS)) {
            for (int i=0; i<NO_OF_THREADS; i++) {
                threadPool.execute(() -> {
                    for (int j=0; j<ITEMS_PER_THREADS; j++) {
                        queue.queue(1);
                    }
                });
            }
        }
        System.out.println(queue.getClass().getName() + " queue size: " + queue.size());

        LongAdder counter = new LongAdder();
        try (ExecutorService threadPool = Executors.newFixedThreadPool(NO_OF_THREADS)) {
            for (int i=0; i<NO_OF_THREADS; i++) {
                threadPool.execute(() -> {
                    while (queue.deque().isPresent()) {
                        counter.increment();
                    }
                });
            }
        }
        System.out.println(queue.getClass().getName() + ": Total elements dequeued: " + counter);
    }
}
