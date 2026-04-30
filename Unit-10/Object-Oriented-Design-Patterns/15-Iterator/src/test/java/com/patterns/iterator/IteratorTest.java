package com.patterns.iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Iterator pattern — Social Media Feed.
 */
class IteratorTest {

    private SocialFeed feed;

    @BeforeEach
    void setUp() {
        feed = new SocialFeed();
        feed.addPost("alice", "First post",  100);
        feed.addPost("bob",   "Second post",  50);
        feed.addPost("alice", "Third post",  200);
    }

    @Test
    @DisplayName("chronologicalIterator returns all posts")
    void chronoReturnsAll() {
        List<Post> posts = drain(feed.chronologicalIterator());
        assertEquals(3, posts.size());
    }

    @Test
    @DisplayName("engagementIterator returns posts in descending like order")
    void engagementDescending() {
        List<Post> posts = drain(feed.engagementIterator());
        assertTrue(posts.get(0).likes() >= posts.get(1).likes());
        assertTrue(posts.get(1).likes() >= posts.get(2).likes());
    }

    @Test
    @DisplayName("authorIterator returns only matching-author posts")
    void authorFilter() {
        List<Post> alicePosts = drain(feed.authorIterator("alice"));
        assertEquals(2, alicePosts.size());
        alicePosts.forEach(p -> assertEquals("alice", p.author()));
    }

    @Test
    @DisplayName("authorIterator for unknown author returns empty")
    void unknownAuthorEmpty() {
        assertFalse(feed.authorIterator("nobody").hasNext());
    }

    private <T> List<T> drain(Iterator<T> it) {
        List<T> result = new ArrayList<>();
        it.forEachRemaining(result::add);
        return result;
    }
}
