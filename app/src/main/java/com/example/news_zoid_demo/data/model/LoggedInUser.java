package com.example.news_zoid_demo.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String userId;
    private String jwtToken;

    public LoggedInUser(String userId, String jwtToken) {
        this.userId = userId;
        this.jwtToken = jwtToken;
    }

    public String getUserId() {
        return userId;
    }

    public String getJwtToken() {
        return jwtToken;
    }
}
