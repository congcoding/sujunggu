package com.sujunggu.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, PostPK> {

    @Query("SELECT p FROM Post p")
    List<Post> findAll();
}
