package threadsafequeue;

import java.util.Optional;

public interface BaseQueue<T> {
    void queue(T element);
    Optional<T> deque();
    int size();
}
