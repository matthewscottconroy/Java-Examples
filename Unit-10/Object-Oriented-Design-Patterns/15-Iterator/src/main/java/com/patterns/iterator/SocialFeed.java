package com.patterns.iterator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Aggregate — a social media feed that can be traversed in multiple ways.
 *
 * <p>The feed stores posts in insertion order. Clients can ask for:
 * <ul>
 *   <li>{@link #chronologicalIterator()} — newest post first</li>
 *   <li>{@link #engagementIterator()} — most-liked post first</li>
 *   <li>{@link #authorIterator(String)} — posts by a specific author only</li>
 * </ul>
 *
 * <p>The collection never exposes its internal list. The client code uses only
 * standard {@link Iterator} and {@code for-each} — it doesn't know (or care)
 * how the feed is stored.
 */
public class SocialFeed {

    private final List<Post> posts = new ArrayList<>();

    /**
     * Adds a post to the feed.
     *
     * @param author  the posting account
     * @param content the post text
     * @param likes   the initial like count
     */
    public void addPost(String author, String content, int likes) {
        int id = posts.size() + 1;
        posts.add(new Post(id, author, content, LocalDateTime.now(), likes));
    }

    /**
     * Returns an iterator that yields posts newest-first.
     *
     * @return a reverse-chronological iterator
     */
    public Iterator<Post> chronologicalIterator() {
        List<Post> sorted = new ArrayList<>(posts);
        sorted.sort(Comparator.comparing(Post::postedAt).reversed());
        return sorted.iterator();
    }

    /**
     * Returns an iterator that yields posts in descending like count.
     *
     * @return an engagement-ranked iterator
     */
    public Iterator<Post> engagementIterator() {
        List<Post> sorted = new ArrayList<>(posts);
        sorted.sort(Comparator.comparingInt(Post::likes).reversed());
        return sorted.iterator();
    }

    /**
     * Returns an iterator that yields only posts by the specified author.
     *
     * @param author the author to filter by
     * @return a filtered iterator
     */
    public Iterator<Post> authorIterator(String author) {
        return posts.stream()
                    .filter(p -> p.author().equalsIgnoreCase(author))
                    .iterator();
    }

    /** @return total number of posts in the feed */
    public int size() { return posts.size(); }
}
