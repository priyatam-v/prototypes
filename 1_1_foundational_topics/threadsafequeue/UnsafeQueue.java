package threadsafequeue;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class UnsafeQueue<T> implements BaseQueue<T> {
    private Queue<T> queue = new LinkedList<>();

    @Override
    public void queue(T element) {
        queue.add(element);
    }

    @Override
    public Optional<T> deque() {
        return Optional.ofNullable(queue.poll());
    }

    @Override
    public int size() {
        return queue.size();
    }
}
