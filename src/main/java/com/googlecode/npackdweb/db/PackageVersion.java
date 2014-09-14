package com.googlecode.npackdweb.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.annotation.AlsoLoad;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Entity;

/**
 * A package version.
 */
@Entity
@Cached
public class PackageVersion {
	/**
	 * this value can be stored in downloadCheckError to prevent the automatic
	 * download check for the binary
	 */
	public static final String DONT_CHECK_THIS_DOWNLOAD =
			"@@@Don't check this download@@@";

	private List<Object> fileContents = new ArrayList<Object>();

	/** abc@2.4 */
	@Id
	public String name = "";

	@AlsoLoad("package")
	public String package_ = "";

	/** only the version number */
	public String version = "";

	public boolean oneFile;
	public String url = "";
	public String sha1 = "";
	public String detectMSI = "";
	public List<String> importantFileTitles = new ArrayList<String>();
	public List<String> importantFilePaths = new ArrayList<String>();
	public List<String> filePaths = new ArrayList<String>();
	public List<String> dependencyPackages = new ArrayList<String>();
	public List<String> dependencyVersionRanges = new ArrayList<String>();
	public List<String> dependencyEnvVars = new ArrayList<String>();
	public List<String> detectFilePaths = new ArrayList<String>();
	public List<String> detectFileSHA1s = new ArrayList<String>();
	public List<String> tags = new ArrayList<String>();

	/** can be null if the check was not yet performed */
	public Date downloadCheckAt;

	/** error message or null if none */
	public String downloadCheckError;

	/** last modification date */
	public Date lastModifiedAt = new Date();

	/** user for the last modification */
	public User lastModifiedBy;

	/** true if this package version was reviewed by the admin and is secure */
	public boolean reviewed;

	/**
	 * For Objectify.
	 */
	public PackageVersion() {
	}

	/**
	 * @param package_
	 *            full internal package name
	 * @param version
	 *            version number
	 */
	public PackageVersion(String package_, String version) {
		this.name = package_ + "@" + version;
		this.package_ = package_;
		this.version = version;
		UserService us = UserServiceFactory.getUserService();
		if (us.isUserLoggedIn())
			this.lastModifiedBy = us.getCurrentUser();
		else
			this.lastModifiedBy =
					new User("tim.lebedkov@gmail.com", "gmail.com");
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getPackage() {
		return package_;
	}

	public boolean getOneFile() {
		return oneFile;
	}

	public String getUrl() {
		return url;
	}

	public String getSha1() {
		return sha1;
	}

	public String getDetectMSI() {
		return detectMSI;
	}

	public List<String> getDependencyPackages() {
		return dependencyPackages;
	}

	public List<String> getDependencyVersionRanges() {
		return dependencyVersionRanges;
	}

	/**
	 * @return copy of this object
	 */
	public PackageVersion copy() {
		PackageVersion c = new PackageVersion();
		c.name = this.name;
		c.package_ = this.package_;
		c.version = this.version;
		c.oneFile = this.oneFile;
		c.url = this.url;
		c.sha1 = this.sha1;
		c.detectMSI = this.detectMSI;
		c.importantFileTitles.addAll(this.importantFileTitles);
		c.importantFilePaths.addAll(this.importantFilePaths);
		c.filePaths.addAll(this.filePaths);
		c.fileContents.addAll(this.fileContents);
		c.dependencyPackages.addAll(this.dependencyPackages);
		c.dependencyVersionRanges.addAll(this.dependencyVersionRanges);
		c.dependencyEnvVars.addAll(this.dependencyEnvVars);
		c.detectFilePaths.addAll(this.detectFilePaths);
		c.detectFileSHA1s.addAll(this.detectFileSHA1s);
		c.tags.addAll(this.tags);
		c.downloadCheckAt = this.downloadCheckAt;
		c.downloadCheckError = this.downloadCheckError;
		c.reviewed = this.reviewed;
		c.lastModifiedBy = this.lastModifiedBy;
		return c;
	}

	/**
	 * Creates <version>
	 * 
	 * @param d
	 *            XML document
	 * @return <version>
	 */
	public Element toXML(Document d) {
		PackageVersion pv = this;

		Element version = d.createElement("version");
		version.setAttribute("name", pv.version);
		version.setAttribute("package", pv.package_);
		if (pv.oneFile)
			version.setAttribute("type", "one-file");
		for (int i = 0; i < pv.importantFilePaths.size(); i++) {
			Element importantFile = d.createElement("important-file");
			version.appendChild(importantFile);
			importantFile.setAttribute("path", pv.importantFilePaths.get(i));
			importantFile.setAttribute("title", pv.importantFileTitles.get(i));
		}
		for (int i = 0; i < pv.filePaths.size(); i++) {
			Element file = d.createElement("file");
			version.appendChild(file);
			file.setAttribute("path", pv.filePaths.get(i));
			NWUtils.t(file, pv.getFileContents(i));
		}
		if (!pv.url.isEmpty())
			NWUtils.e(version, "url", pv.url);

		String sha1 = pv.sha1.trim();
		if (!sha1.isEmpty()) {
			if (sha1.length() == 40)
				NWUtils.e(version, "sha1", sha1);
			else if (sha1.length() == 64)
				NWUtils.e(version, "hash-sum", "type", "SHA-256", sha1);
		}

		for (int i = 0; i < pv.dependencyPackages.size(); i++) {
			Element dependency = d.createElement("dependency");
			version.appendChild(dependency);
			dependency.setAttribute("package", pv.dependencyPackages.get(i));
			dependency.setAttribute("versions",
					pv.dependencyVersionRanges.get(i));
			if (!pv.dependencyEnvVars.get(i).isEmpty())
				NWUtils.e(dependency, "variable", pv.dependencyEnvVars.get(i));
		}
		if (!pv.detectMSI.isEmpty())
			NWUtils.e(version, "detect-msi", pv.detectMSI);
		for (int i = 0; i < pv.detectFilePaths.size(); i++) {
			Element detectFile = d.createElement("detect-file");
			version.appendChild(detectFile);
			NWUtils.e(detectFile, "path", pv.detectFilePaths.get(i));
			NWUtils.e(detectFile, "sha1", pv.detectFileSHA1s.get(i));
		}
		return version;
	}

	/**
	 * @param i
	 *            index of the file
	 * @return file contents <file>
	 */
	public String getFileContents(int i) {
		Object obj = fileContents.get(i);
		if (obj instanceof Text)
			return ((Text) obj).getValue();
		else
			return (String) obj;
	}

	@PostLoad
	public void postLoad() {
		if (this.sha1 == null)
			this.sha1 = "";

		// Bugfix: the content was stored as <String> which lead to the
		// conversion of long strings (> 500 characters) to Text and changing
		// their position in the list
		for (int i = 0; i < this.fileContents.size(); i++) {
			Object obj = this.fileContents.get(i);
			if (obj instanceof String)
				this.fileContents.set(i, new Text((String) obj));
		}

		while (this.fileContents.size() < this.filePaths.size())
			this.fileContents.add(new Text(""));

		if (this.lastModifiedAt == null)
			this.lastModifiedAt = new Date();

		while (this.dependencyEnvVars.size() < this.dependencyPackages.size())
			this.dependencyEnvVars.add("");

		if (lastModifiedBy == null) {
			UserService us = UserServiceFactory.getUserService();
			if (us.isUserLoggedIn())
				this.lastModifiedBy = us.getCurrentUser();
			else
				this.lastModifiedBy =
						new User("tim.lebedkov@gmail.com", "gmail.com");
		}
	}

	@PrePersist
	void onPersist() {
		NWUtils.incDataVersion();
	}

	/**
	 * Removes the file with the specified index.
	 * 
	 * @param index
	 *            index of the file
	 */
	public void removeFile(int index) {
		this.filePaths.remove(index);
		this.fileContents.remove(index);
	}

	/**
	 * Changes the content of the specified <file>
	 * 
	 * @param index
	 *            index of the file
	 * @param content
	 *            file content
	 */
	public void setFileContents(int index, String content) {
		this.fileContents.set(index, new Text(content));
	}

	/**
	 * Adds a new <file>
	 * 
	 * @param path
	 *            file path
	 * @param content
	 *            file content
	 */
	public void addFile(String path, String content) {
		this.filePaths.add(path);
		this.fileContents.add(new Text(content));
	}

	/**
	 * @return created Key for this object
	 */
	public Key<PackageVersion> createKey() {
		return new Key<PackageVersion>(PackageVersion.class, this.name);
	}

	/**
	 * Removes all defined text files.
	 */
	public void clearFiles() {
		this.filePaths.clear();
		this.fileContents.clear();
	}

	/**
	 * @return the number of text files.
	 */
	public int getFileCount() {
		return this.filePaths.size();
	}

	/**
	 * Searches for a package version.
	 * 
	 * @param ofy
	 *            Objectify
	 * @param packageName
	 *            full package name
	 * @param v
	 *            version number
	 * @return found version or null
	 */
	public static PackageVersion find(Objectify ofy, String packageName,
			String v) {
		return ofy.find(new Key<PackageVersion>(PackageVersion.class,
				packageName + "@" + v));
	}

	/**
	 * @param ofy
	 *            Objectify instance
	 * @return first 20 package versions with errors downloading the binary
	 */
	public static List<PackageVersion> find20NotReviewed(Objectify ofy) {
		return ofy.query(PackageVersion.class).limit(20)
				.filter("reviewed !=", true).list();
	}

	/**
	 * @param checkSum
	 *            true = also check SHA-1 or SHA-256
	 * @param algorithm
	 *            SHA-256 or SHA-1
	 * @return info about the download or null if the download failed
	 */
	public NWUtils.Info check(boolean checkSum, String algorithm) {
		NWUtils.Info info = null;
		this.downloadCheckAt = new Date();
		this.downloadCheckError = "Unknown error";
		if (!this.url.isEmpty()) {
			try {
				info = NWUtils.download(this.url, algorithm);
				if (checkSum) {
					if (this.sha1.trim().isEmpty())
						this.downloadCheckError = null;
					else {
						String sha1_ = NWUtils.byteArrayToHexString(info.sha1);
						if (sha1_.equalsIgnoreCase(this.sha1.trim())) {
							this.downloadCheckError = null;
						} else {
							this.downloadCheckError =
									"Wrong SHA1: " + this.sha1 +
											" was expected, but " + sha1_ +
											" was found";
						}
					}
				} else {
					this.downloadCheckError = null;
				}
			} catch (Exception e) {
				this.downloadCheckError =
						"Error downloading: " + e.getMessage();
			}
		} else {
			this.downloadCheckError = null;
		}
		return info;
	}

	/**
	 * Adds a dependency
	 * 
	 * @param package_
	 *            depends on this package
	 * @param versions
	 *            versions range like "[9, 10)"
	 */
	public void addDependency(String package_, String versions) {
		addDependency(package_, versions, "");
	}

	/**
	 * Adds a dependency
	 * 
	 * @param package_
	 *            depends on this package
	 * @param versions
	 *            versions range like "[9, 10)"
	 * @param envVar
	 *            name of the environment variable or ""
	 */
	public void addDependency(String package_, String versions, String envVar) {
		this.dependencyPackages.add(package_);
		this.dependencyVersionRanges.add(versions);
		this.dependencyEnvVars.add(envVar);
	}
}
