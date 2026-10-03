package requesthedging;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Demo {
    private static final int NUM_OF_THREADS = 100;
    public static void main(String[] args) {
//        System.out.println("Fetching blog details using Naive approach");
//        BlogServiceNaive blogServiceNaive = new BlogServiceNaive();
//        try (ExecutorService threadPool = Executors.newFixedThreadPool(NUM_OF_THREADS)) {
//            for (int i=0; i<NUM_OF_THREADS; i++) {
//                String blogId = "123";
//                threadPool.execute(() -> {
//                    blogServiceNaive.getBlog(blogId);
//                    System.out.println("Fetched blog details for id: " + blogId);
//                });
//            }
//        }

        System.out.println("==========================\n");
        System.out.println("Fetching blog details using Request Hedging");
        BlogServiceWithRequestHedging blogServiceWithRequestHedging = new BlogServiceWithRequestHedging();
        try (ExecutorService threadPool = Executors.newFixedThreadPool(NUM_OF_THREADS)) {
            for (int i=0; i<NUM_OF_THREADS; i++) {
                String blogId = "123";
                threadPool.execute(() -> {
                    blogServiceWithRequestHedging.getBlog(blogId);
                    System.out.println("Fetched blog details for id: " + blogId);
                });
            }
        }

    }
}
