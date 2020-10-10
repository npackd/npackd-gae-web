package com.googlecode.npackdweb;

/**
 * A user
 */
public class User {

    /**
     * email
     */
    public String email;

    /**
     * Server
     */
    public String server;

    /**
     * Constructor
     *
     * @param email email
     * @param server server
     */
    public User(String email, String server) {
        this.email = email;
        this.server = server;
    }

    /**
     * @return user nickname
     */
    public String getNickname() {
        return email;
    }

    /**
     * @return email
     */
    public String getEmail() {
        return email;
    }
}
