package ru.netology.repository;

import org.springframework.beans.factory.annotation.Autowired;
import ru.netology.model.Post;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PostRepositoryStubImpl implements PostRepository {
    private static AtomicInteger count = new AtomicInteger(0);
    @Autowired
    private ConcurrentMap<Integer, Post> map;

    public PostRepositoryStubImpl(ConcurrentMap<Integer, Post> concurrentMap) {
        this.map = concurrentMap;
    }


    public List<Post> all() {
        return new ArrayList<>(map.values());
    }

    public Optional<Post> getById(long id) {
        return map.entrySet().stream().
                filter(x -> x.getKey() == id).
                map(Map.Entry::getValue).
                findAny();
    }

    public Post save(Post post) {
        if (post.getId() == 0) {
            map.put(count.addAndGet(1), post);
            post.setId(count.get());
        } else {
            Post mapPost = map.computeIfPresent((int) post.getId(), (key, value) -> post);
            if (mapPost == null) {
                System.out.println("Ошибка обновления данных. ");
                return null;
            }
        }
        return post;
    }

    public void removeById(long id) {
        map.remove((int) id);
    }
}
