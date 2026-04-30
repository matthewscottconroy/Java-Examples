package com.patterns.iterator;

import java.time.LocalDateTime;

/**
 * A social media post — the element type in the feed collection.
 *
 * @param id          unique post identifier
 * @param author      the account that posted
 * @param content     the post text
 * @param postedAt    when the post was created
 * @param likes       number of likes
 */
public record Post(int id, String author, String content,
                   LocalDateTime postedAt, int likes) {

    @Override
    public String toString() {
        return String.format("[#%d @%s ♥%d] %s",
                id, author, likes, content.substring(0, Math.min(60, content.length())));
    }
}
