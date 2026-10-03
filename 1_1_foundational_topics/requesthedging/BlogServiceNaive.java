package requesthedging;

import java.util.concurrent.ConcurrentHashMap;

public class BlogServiceNaive implements BlogService {
    ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    private String getBlogFromDatabase(String blogId) {
        System.out.println("Fetching blog details from db for id: " + blogId);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return blogId + " blog details";
    }

    @Override
    public String getBlog(String blogId) {
        String blogFromCache = cache.get(blogId);
        if (blogFromCache != null) {
            System.out.println("Fetching blog details from cache for id: " + blogId);
            return blogFromCache;
        }

        String blogFromDatabase = getBlogFromDatabase(blogId);
        cache.putIfAbsent(blogId, blogFromDatabase);
        return blogFromDatabase;
    }
}
