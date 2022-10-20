package ru.netology.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.netology.controller.PostController;
import ru.netology.model.Post;
import ru.netology.repository.PostRepository;
import ru.netology.repository.PostRepositoryStubImpl;
import ru.netology.service.PostService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class JavaConfig {
    @Bean
    // название метода - название бина
    public PostController postController() {
        // вызов метода и есть DI
        return new PostController(postService());
    }

    @Bean
    public PostService postService() {
        return new PostService(postRepository());
    }

    @Bean
    public PostRepository postRepository() {
        return new PostRepositoryStubImpl();
    }

    @Bean
    public ConcurrentHashMap<Integer, Post> concurrentHashMap() {
        return new ConcurrentHashMap<>();
    }
}
