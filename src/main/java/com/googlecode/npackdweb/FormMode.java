package com.googlecode.npackdweb;

/**
 * Different form modes
 */
public enum FormMode {
    /** view existing data */
    VIEW,

    /** edit existing data */
    EDIT,

    /** create new object */
    CREATE;

    /**
     * @return true if this mode allows editing
     */
    public boolean isEditable() {
        return this == EDIT || this == CREATE;
    }
}
