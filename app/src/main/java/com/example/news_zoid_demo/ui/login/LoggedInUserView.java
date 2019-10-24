package com.example.news_zoid_demo.ui.login;

/**
 * Class exposing authenticated user details to the UI.
 */
class LoggedInUserView {
    private String displayName;

    private String jwtToken;
    //... other data fields that may be accessible to the UI

    LoggedInUserView(String displayName, String jwtToken) {
        this.displayName = displayName;
        this.jwtToken = jwtToken;
    }

    String getDisplayName() {
        return displayName;
    }

    public String getJwtToken() {
        return jwtToken;
    }
}
