package com.googlecode.npackdweb.db;

/**
 * Package architectures.
 */
public enum Arch {
    ANY,
    I686,
    X86_64;

    /**
     * Converts a number stored in the datastore for Package.arch to a value of
     * this enum.
     *
     * @param v value stored in the datastore
     * @return an enum
     */
    public static Arch fromDatastore(long v) {
        Arch r;
        if (v == 2) {
            r = X86_64;
        } else if (v == 1) {
            r = I686;
        } else {
            r = ANY;
        }
        return r;
    }

    /**
     * Converts a value from a repository XML to a value of this enum.
     *
     * @param v value from XML
     * @return an enum
     */
    public static Arch fromString(final String v) {
        Arch r;
        if (v.equals("x86_64")) {
            r = X86_64;
        } else if (v.equals("i686")) {
            r = I686;
        } else {
            r = ANY;
        }
        return r;
    }

    /**
     * Converts this enum to a value stored in the datastore for Package.arch.
     *
     * @return value stored in the datastore
     */
    public long toDatastore() {
        long r;
        if (this == X86_64) {
            r = 2;
        } else if (this == I686) {
            r = 1;
        } else {
            r = 0;
        }
        return r;
    }

    @Override
    public String toString() {
        String r;
        if (this == X86_64) {
            r = "x86_64";
        } else if (this == I686) {
            r = "i686";
        } else {
            r = "any";
        }
        return r;
    }
}
