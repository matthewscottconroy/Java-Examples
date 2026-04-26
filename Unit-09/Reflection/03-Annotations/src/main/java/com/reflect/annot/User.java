package com.reflect.annot;

/**
 * A domain class whose fields are annotated with validation and mapping metadata.
 */
public class User {

    @NotNull
    @Length(min = 2, max = 50)
    @Column(name = "user_name", nullable = false)
    private String username;

    @NotNull
    @Length(min = 8, message = "password too short")
    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "email_address")
    private String email;   // optional — no @NotNull

    private int age;        // no annotations — unconstrained

    public User(String username, String password, String email, int age) {
        this.username = username;
        this.password = password;
        this.email    = email;
        this.age      = age;
    }

    @Override public String toString() {
        return "User{username=" + username + ", password=" + password
                + ", email=" + email + ", age=" + age + "}";
    }
}
