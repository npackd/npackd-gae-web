package com.googlecode.npackdweb.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.MapJob;
import com.google.appengine.tools.mapreduce.MapReduceSettings;
import com.google.appengine.tools.mapreduce.MapSpecification;
import com.google.appengine.tools.mapreduce.inputs.DatastoreInput;
import com.google.appengine.tools.mapreduce.outputs.NoOutput;
import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

/**
 * Clean the dependencies. This action processes all package versions.
 */
public class CleanDependenciesAction extends Action {
	/**
	 * -
	 */
	public CleanDependenciesAction() {
		super("^/clean-dependencies$", ActionSecurityType.ADMINISTRATOR);
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		MapReduceSettings settings =
				new MapReduceSettings.Builder().setWorkerQueueName("default")
						.setBucketName("npackd").build();

		MapSpecification<Entity, Void, Void> ms =
				new MapSpecification.Builder<>(new DatastoreInput(
						"PackageVersion", 50), new CleanDependenciesMapper(),
						new NoOutput<Void, Void>()).build();
		String jobId = MapJob.start(ms, settings);

		// jobId is used to monitor, see step 5) below
		return new MessagePage("Job ID: " + jobId);
	}
}
