package pessimisticlock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Demo {
    private static final int NO_OF_THREADS = 100;
    private static final int NO_OF_JOBS = 100;

    public static void main(String[] args) throws InterruptedException {
        ReentrantLockCounter reentrantLockCounter = new ReentrantLockCounter();
        UnsafeCounter unsafeCounter = new UnsafeCounter();

        run(unsafeCounter);
        run(reentrantLockCounter);
    }

    private static void run(Counter counter) throws InterruptedException {
        try (ExecutorService threadPool = Executors.newFixedThreadPool(NO_OF_THREADS)) {
            for (int i=0; i<NO_OF_THREADS; i++) {
                threadPool.submit(() -> {
                    for (int j=0; j<NO_OF_JOBS; j++) {
                        counter.increment();
                    }
                });
            }
            threadPool.shutdown();
            if (!threadPool.awaitTermination(1, TimeUnit.MINUTES)) {
                threadPool.shutdownNow();
                throw new IllegalStateException("Worker did not finish the task in 1 minute.");
            }

            System.out.println(counter.getClass().getName() + " counter's final count: " + counter.get());
        }
    }
}
