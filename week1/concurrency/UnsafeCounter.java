package concurrency;

public class UnsafeCounter implements Counter {

    private int counter = 0;

    @Override
    public void increment() {
        counter++;
    }

    @Override
    public int get() {
        return counter;
    }
}
