package blockingqueue;

public class BlockingQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        // Small capacity on purpose, so we can actually SEE blocking happen
//        blockingqueue.BlockingQueueUsingSynchronization<Integer> queue = new blockingqueue.BlockingQueueUsingSynchronization<>(3);
        BlockingQueueUsingReentrant<Integer> queue = new BlockingQueueUsingReentrant<>(3);

        // Producer is FAST (200ms/item) -> will fill the queue and start blocking on put()
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                try {
                    queue.put(i);
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "producer");

        // Consumer is SLOW (600ms/item) -> forces the queue to fill up, so producer blocks
        Thread consumer = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                try {
                    Thread.sleep(600);
                    int item = queue.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "consumer");

        producer.start();
        consumer.start();

        // join() = "main thread, pause here until this other thread has fully finished."
        // Without these two lines, main would race ahead and print "Done" while
        // producer/consumer are still mid-flight on their own threads.
        producer.join();
        consumer.join();

        System.out.println("Done. Final queue size: " + queue.size());
    }
}