package com.googlecode.npackdweb.db;

/**
 * 1.2.3
 *
 * Default constructor creates "1"
 */
public class Version {

    private int[] parts = new int[]{1};

    /**
     * "1"
     */
    public Version() {
    }

    /**
     * -
     *
     * @param major major version number
     * @param minor minor version number
     */
    public Version(int major, int minor) {
        this.parts = new int[]{major, minor};
    }

    /**
     * Parses a string representation.
     *
     * @param v "1.2.3"
     * @return [1, 2, 3]
     * @throws NumberFormatException if something cannot be parsed
     */
    public static Version parse(String v) throws NumberFormatException {
        Version r = new Version();
        String[] parts = v.split("\\.");
        r.parts = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            r.parts[i] = Integer.parseInt(parts[i]);
        }
        return r;
    }

    /**
     * Compares this object with another version. This function also works for
     * non-normalized versions.
     *
     * @param v another version
     * @return 1, -1 or 0
     */
    public int compare(Version v) {
        int r = 0;
        for (int i = 0; i < Math.max(parts.length, v.parts.length); i++) {
            int thisp = i < parts.length ? parts[i] : 0;
            int vp = i < v.parts.length ? v.parts[i] : 0;
            if (thisp < vp) {
                r = -1;
                break;
            } else if (thisp > vp) {
                r = 1;
                break;
            }
        }
        return r;
    }

    /**
     * Removes the trailing zeros.
     */
    public void normalize() {
        int i = this.parts.length - 1;
        while (i > 0) {
            if (this.parts[i] != 0) {
                break;
            }
            i--;
        }
        if (i != this.parts.length - 1) {
            int[] newParts = new int[i + 1];
            System.arraycopy(this.parts, 0, newParts, 0, i + 1);
            this.parts = newParts;
        }
    }

    @Override
    public boolean equals(Object arg0) {
        boolean r = false;

        if (arg0 instanceof Version) {
            Version v = (Version) arg0;
            r = compare(v) == 0;
        }

        return r;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.parts.length; i++) {
            if (i != 0) {
                sb.append('.');
            }
            sb.append(this.parts[i]);
        }
        return sb.toString();
    }

    /**
     * @param index index of the part (0, 1, ...).
     * @return the specified part of the version or 0 if the part is not present
     */
    public int getPart(int index) {
        if (index < parts.length) {
            return parts[index];
        } else {
            return 0;
        }
    }
}
