package com.googlecode.npackdweb;

/**
 * Authentication.
 */
public class AuthService {

    private static final AuthService instance = new AuthService();

    private AuthService() {

    }

    /**
     * @return the only instance of this class
     */
    public static AuthService getInstance() {
        return instance;
    }

    /**
     * @return current user or null
     */
    public User getCurrentUser() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return was the current user authenticated
     */
    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    /**
     * @return the current user is an admin
     */
    public boolean isUserAdmin() {
        User user = getCurrentUser();
        return user != null && user.email == "tim.lebedkov@gmail.com";
    }

    /**
     * @param requestURI forward to this URL after the login
     * @return URL to start the login process
     */
    public String createLoginURL(String requestURI) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @param requestURI forward to this URL after the logout
     * @return URL to start the logout process
     */
    public String createLogoutURL(String requestURI) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
