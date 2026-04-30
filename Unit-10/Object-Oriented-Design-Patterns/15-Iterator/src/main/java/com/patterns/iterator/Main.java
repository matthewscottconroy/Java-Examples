package com.patterns.iterator;

import java.util.Iterator;

/**
 * Demonstrates the Iterator pattern with a social media feed.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Social Media Feed (Iterator Pattern) ===\n");

        SocialFeed feed = new SocialFeed();
        feed.addPost("alice",   "Just launched my new side project!", 342);
        feed.addPost("bob",     "Hot take: tabs > spaces.",             89);
        feed.addPost("carol",   "Beautiful sunset from the office roof.",  1204);
        feed.addPost("alice",   "Follow-up: the project hit 1k stars!",   820);
        feed.addPost("dave",    "Anyone else getting rate-limited by the API?", 55);

        System.out.println("--- Chronological (newest first) ---");
        Iterator<Post> chrono = feed.chronologicalIterator();
        while (chrono.hasNext()) System.out.println("  " + chrono.next());

        System.out.println("\n--- By engagement (most liked first) ---");
        Iterator<Post> engaged = feed.engagementIterator();
        while (engaged.hasNext()) System.out.println("  " + engaged.next());

        System.out.println("\n--- Posts by alice only ---");
        Iterator<Post> alicePosts = feed.authorIterator("alice");
        while (alicePosts.hasNext()) System.out.println("  " + alicePosts.next());

        System.out.println("\nSame feed, three different traversals. Internal structure: hidden.");
    }
}
