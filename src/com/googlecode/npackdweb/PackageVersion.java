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
}
