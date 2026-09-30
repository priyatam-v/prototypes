package fairmultithread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo {
    private static final long MAX_VAL = 100_000;
    private static final int concurrency = 10;

    public static void main(String[] args) {
        // 1. Sequential
        long start1 = System.nanoTime();
        int totalPrimesSimple = 1; // 2 is a prime
        for (int i=3; i<MAX_VAL; i++) {
            if (checkPrime(i))
                totalPrimesSimple++;
        }
        long end1 = System.nanoTime();
        System.out.println("Total no of primes (Simple): " + totalPrimesSimple + ", time taken: " + ((end1 - start1)/1000_000_000.0) + "s");

        // 2. Batches
        long start2 = System.nanoTime();
        AtomicInteger counter2 = new AtomicInteger(1); //  2 is a prime
        try (ExecutorService threadPool = Executors.newFixedThreadPool(concurrency)) {
            for (int i=0; i<concurrency; i++) {
                long batchSize = (MAX_VAL + concurrency - 1) / concurrency;
                long from = Math.max(3, (i * batchSize) + 1);
                long to = Math.min((i + 1) * batchSize, MAX_VAL);
                threadPool.execute(() -> {
                    for (long j=from; j<=to; j++) {
                        if (checkPrime(j)) counter2.incrementAndGet();
                    }
                });
            }
        }
        long end2 = System.nanoTime();
        System.out.println("Total no of primes (Concurrent): " + counter2.intValue() + ", time taken: " + ((end2 - start2)/1000_000_000.0) + "s");

        // 3. Dynamic based on available threads
        long start3 = System.nanoTime();
        AtomicInteger counter3 = new AtomicInteger(1); //  2 is a prime
        AtomicInteger current = new AtomicInteger(3);

        try (ExecutorService threadPool = Executors.newFixedThreadPool(concurrency)) {
            for (int i=0; i<concurrency; i++) {
                threadPool.execute(() -> {
                    int currentNumber;
                    while ((currentNumber = current.getAndIncrement()) <= MAX_VAL) {
                        if (checkPrime(currentNumber))
                            counter3.incrementAndGet();
                    }
                });
            }
        }

        long end3 = System.nanoTime();
        System.out.println("Total no of primes (Dynamic): " + counter3.intValue() + ", time taken: " + ((end3 - start3)/1000_000_000.0) + "s");
    }

    private static boolean checkPrime(long n) {
        if (n < 2) return false;
        for (long i=2; i<n; i++)
            if (n % i == 0) return false;
        return true;
    }
}
