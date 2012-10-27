package com.googlecode.npackdweb;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.AlsoLoad;
import com.googlecode.objectify.annotation.Entity;

/**
 * A package version.
 */
@Entity
public class PackageVersion {
	@Id
	String name = "";
	@AlsoLoad("package")
	String package_ = "";
	String version = "";
	boolean oneFile;
	String url = "";
	String sha1 = "";
	String detectMSI = "";
	List<String> importantFileTitles = new ArrayList<String>();
	List<String> importantFilePaths = new ArrayList<String>();
	List<String> filePaths = new ArrayList<String>();
	List<String> fileContents = new ArrayList<String>();
	List<String> dependencyPackages = new ArrayList<String>();
	List<String> dependencyVersionRanges = new ArrayList<String>();
	List<String> dependencyEnvVars = new ArrayList<String>();
	List<String> detectFilePaths = new ArrayList<String>();
	List<String> detectFileSHA1s = new ArrayList<String>();
	List<String> tags = new ArrayList<String>();

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
		return c;
	}
}
