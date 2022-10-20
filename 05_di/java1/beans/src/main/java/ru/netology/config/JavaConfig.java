package ru.netology.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.netology.controller.PostController;
import ru.netology.model.Post;
import ru.netology.repository.PostRepository;
import ru.netology.repository.PostRepositoryStubImpl;
import ru.netology.service.PostService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Configuration
public class JavaConfig {
    @Bean
    // аргумент метода и есть DI
    // название метода - название бина
    public PostController postController(PostService service) {
        return new PostController(service);
    }

    @Bean
    public PostService postService(PostRepository repository) {
        return new PostService(repository);
    }

    @Bean
    public PostRepository postRepository(ConcurrentMap<Integer, Post> map) {
        return new PostRepositoryStubImpl(map);
    }

    @Bean
    public ConcurrentMap<Integer, Post> concurrentMap() {
        return new ConcurrentHashMap<>();
    }
}
