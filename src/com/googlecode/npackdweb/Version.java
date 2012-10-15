package com.googlecode.npackdweb;

/**
 * 1.2.3
 */
public class Version {
	private int parts[] = new int[] { 1 };

	/**
	 * Parses a string representation.
	 * 
	 * @param v
	 *            "1.2.3"
	 * @return [1, 2, 3]
	 * @throws NumberFormatException
	 *             if something cannot be parsed
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
	 * Compares this object with another version
	 * 
	 * @param v
	 *            another version
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
}
