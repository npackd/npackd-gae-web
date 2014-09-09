package com.googlecode.npackdweb.wlib;

/**
 * Security type for an action
 */
public enum ActionSecurityType {
    /** everybody can call this action */
    ANONYMOUS,

    /** only logged in users can call this action */
    LOGGED_IN,

    /** only administrators can call this action */
    ADMINISTRATOR
}
