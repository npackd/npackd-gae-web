package com.googlecode.npackdweb;

/**
 * Dependency on another package.
 */
public class Dependency {
	/** full package name of the dependency */
	public String package_ = "";

	/** lower bound */
	public Version min;

	/** upper bound */
	public Version max;

	/** true = lower bound included */
	public boolean minIncluded;

	/** true = upper bound included */
	public boolean maxIncluded;

	/**
	 * [0.0, 1.0)
	 */
	public Dependency() {
		this.min = new Version(0, 0);
		minIncluded = true;
		this.max = new Version(1, 0);
		maxIncluded = false;
	}

	/**
	 * Parses the versions.
	 * 
	 * @param versions
	 * @return error message or null
	 */
	public String setVersions(String versions) {
		String err = null;

		String versions_ = versions.trim();

		if (versions.length() < 5) {
			err = "Version range is too short";
		}

		boolean minIncluded_ = true;
		boolean maxIncluded_ = false;

		// qDebug() << "Repository::createDependency.1" << versions;

		if (err == null) {
			if (versions_.startsWith("["))
				minIncluded_ = true;
			else if (versions_.startsWith("("))
				minIncluded_ = false;
			else
				err = "Version range starts with " + versions_.charAt(0);
		}

		if (err == null) {
			versions_ = versions_.substring(1);
		}

		// qDebug() << "Repository::createDependency.1.1" << versions;

		if (err == null) {
			if (versions_.endsWith("]"))
				maxIncluded_ = true;
			else if (versions_.endsWith(")"))
				maxIncluded_ = false;
			else
				err =
						"Version range ends with " +
								versions_.charAt(versions_.length() - 1);
		}
		if (err == null) {
			versions_ = versions_.substring(0, versions_.length() - 1);
		}
		// qDebug() << "Repository::createDependency.2";
		String[] parts = null;
		if (err == null) {
			parts = versions_.split(",");
			if (parts.length != 2) {
				err = "There must be exactly 2 parts separated by a comma";
			}
		}

		Version min_ = null;
		Version max_ = null;
		if (err == null) {
			try {
				min_ = Version.parse(parts[0].trim());
				max_ = Version.parse(parts[1].trim());
			} catch (NumberFormatException e) {
				err = e.getMessage();
			}
		}

		if (err == null) {
			this.minIncluded = minIncluded_;
			this.min = min_;
			this.maxIncluded = maxIncluded_;
			this.max = max_;
		}

		return err;
	}

	@Override
	public boolean equals(Object obj) {
		boolean r = false;
		if (obj instanceof Dependency) {
			Dependency d = (Dependency) obj;
			r =
					this.package_.equals(d.package_) &&
							this.minIncluded == d.minIncluded &&
							this.maxIncluded == d.maxIncluded &&
							this.min.equals(d.min) && this.max.equals(d.max);

		}
		return r;
	}
}