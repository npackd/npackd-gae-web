package com.googlecode.npackdweb.admin;

import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.Dependency;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

public class CleanDependenciesMapper extends MapOnlyMapper<Entity, Void> {
	private static final long serialVersionUID = 1L;

	private transient DatastoreMutationPool pool;

	@Override
	public void beginSlice() {
		this.pool = DatastoreMutationPool.create();
	}

	@Override
	public void endSlice() {
		this.pool.flush();
	}

	@Override
	public void map(Entity value) {
		removeNpackdCLCalls(value);
		return;
		/*
		 * Objectify ofy = DefaultServlet.getObjectify();
		 * 
		 * Version one = new Version();
		 * 
		 * PackageVersion pv = ofy.find(new
		 * Key<PackageVersion>(value.getKey())); boolean save = false; for (int
		 * i = 0; i < pv.dependencyPackages.size(); i++) { String p =
		 * pv.dependencyPackages.get(i); String r =
		 * pv.dependencyVersionRanges.get(i); String var =
		 * pv.dependencyEnvVars.get(i);
		 * 
		 * if (p.equals("com.googlecode.windows-package-manager.NpackdCL") &&
		 * var.trim().equals("")) { Dependency d = new Dependency(); String err
		 * = d.setVersions(r); if (err != null) break;
		 * 
		 * if (d.min.equals(one) && d.max.equals(one)) {
		 * System.out.println(pv.package_ + " " + pv.version + " " + p + " " +
		 * r);
		 * 
		 * pv.dependencyPackages.remove(i);
		 * pv.dependencyVersionRanges.remove(i); pv.dependencyEnvVars.remove(i);
		 * save = true; break; } } }
		 * 
		 * for (int i = 0; i < pv.getFileCount(); i++) { String s =
		 * pv.getFileContents(i); List<String> lines = NWUtils.splitLines(s);
		 * for (int j = 0; j < lines.size();) { String line = lines.get(j); if
		 * (line.trim() .toLowerCase() .equals(
		 * "if \"%npackd_cl%\" equ \"\" set npackd_cl=..\\com.googlecode.windows-package-manager.npackdcl-1"
		 * )) { lines.remove(j); pv.setFileContents(i, NWUtils.join("\r\n",
		 * lines)); save = true; } else { j++; } } }
		 * 
		 * if (save) { System.out.println("Saving " + pv.name);
		 * NWUtils.savePackageVersion(ofy, pv, true, false); }
		 */
	}

	public void removeNpackdCLCalls(Entity value) {
		Objectify ofy = DefaultServlet.getObjectify();

		PackageVersion pv = ofy.find(new Key<PackageVersion>(value.getKey()));
		boolean save = false;

		for (int i = 0; i < pv.getFileCount(); i++) {
			String s = pv.getFileContents(i);
			List<String> lines = NWUtils.splitLines(s);
			for (int j = 0; j < lines.size() - 1;) {
				String line = lines.get(j);
				String line2 = lines.get(j + 1);
				String[] npackdCLParams = getNpackdCLParams(line, line2);
				boolean found = false;
				if (npackdCLParams != null) {
					Dependency d = new Dependency();
					if (d.setVersions(npackdCLParams[1]) == null) {
						d.package_ = npackdCLParams[0];
						int index = pv.findDependency(d);
						if (index >= 0 &&
								(pv.dependencyEnvVars.get(index).isEmpty() || pv.dependencyEnvVars
										.get(index).equals(npackdCLParams[2]))) {
							lines.remove(j);
							lines.remove(j);
							pv.dependencyEnvVars.set(index, npackdCLParams[2]);
							pv.setFileContents(i, NWUtils.join("\r\n", lines));
							found = true;
							save = true;
						}
					}
				}

				if (!found) {
					j++;
				}
			}
		}

		if (save) {
			System.out.println("Saving " + pv.name);
			NWUtils.savePackageVersion(ofy, pv, true, false);
		}
	}

	private static String[] getNpackdCLParams(String line, String line2) {
		final String prefix =
				"set onecmd=\"%npackd_cl%\\npackdcl.exe\" \"path\" \"--package=";
		final String prefix2 =
				"for /f \"usebackq delims=\" %%x in (`%%onecmd%%`) do set ";
		String[] result = null;
		if (line.trim().toLowerCase().startsWith(prefix) &&
				line2.trim().toLowerCase().startsWith(prefix2)) {
			String[] parts = line.substring(prefix.length()).split("\"");
			String[] parts2 = line2.substring(prefix2.length()).split("=");
			if (parts.length > 2 && parts2.length > 0) {
				String p = parts[0].trim();
				String vs = parts[2].trim();
				if (vs.startsWith("--versions=")) {
					Dependency d = new Dependency();
					if (d.setVersions(vs.substring(11)) == null) {
						String var = parts2[0].trim();
						result = new String[] { p, vs.substring(11), var };
					}
				}
			}
		}

		return result;
	}
}