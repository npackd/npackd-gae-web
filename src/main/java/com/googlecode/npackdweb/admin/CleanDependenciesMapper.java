package com.googlecode.npackdweb.admin;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.googlecode.npackdweb.DefaultServlet;
import com.googlecode.npackdweb.Dependency;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.Version;
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
		Objectify ofy = DefaultServlet.getObjectify();

		Version one = new Version();

		PackageVersion pv = ofy.find(new Key<PackageVersion>(value.getKey()));
		for (int i = 0; i < pv.dependencyPackages.size(); i++) {
			String p = pv.dependencyPackages.get(i);
			String r = pv.dependencyVersionRanges.get(i);
			String var = pv.dependencyEnvVars.get(i);

			if (p.equals("com.googlecode.windows-package-manager.NpackdCL") &&
					var.trim().equals("")) {
				Dependency d = new Dependency();
				String err = d.setVersions(r);
				if (err != null)
					break;

				if (d.min.equals(one) && d.max.equals(one)) {
					System.out.println(pv.package_ + " " + pv.version + " " +
							p + " " + r);

					pv.dependencyPackages.remove(i);
					pv.dependencyVersionRanges.remove(i);
					pv.dependencyEnvVars.remove(i);
					NWUtils.savePackageVersion(ofy, pv, true);
					break;
				}
			}
		}
	}
}