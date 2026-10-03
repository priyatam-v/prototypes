package requesthedging;

public interface BlogService {
    String getBlog(String blogId) throws InterruptedException;
}
