package requesthedging;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BlogServiceWithRequestHedging implements BlogService {
    ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, CompletableFuture<String>> inFlightBlogsMap = new ConcurrentHashMap<>();

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

        CompletableFuture<String> currentBlog = new CompletableFuture<>();
        CompletableFuture<String> existingBlog = inFlightBlogsMap.putIfAbsent(blogId, currentBlog);
        if (existingBlog != null) {
            return existingBlog.join(); // followers waiting for the leader to complete request.
        }

        try {
            String blogFromDatabase = getBlogFromDatabase(blogId);
            cache.put(blogId, blogFromDatabase);
            currentBlog.complete(blogFromDatabase);
            return blogFromDatabase;
        } catch (Exception ex) {
            currentBlog.completeExceptionally(ex);
            throw ex;
        } finally {
            inFlightBlogsMap.remove(blogId);
        }
    }
}
