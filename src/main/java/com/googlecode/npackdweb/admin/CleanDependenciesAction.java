package com.googlecode.npackdweb.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.Page;

public class CleanDependenciesAction extends Action {

	public CleanDependenciesAction(String urlRegEx) {
		super(urlRegEx);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		// These settings are covered in step 4) below
		/*
		 * MapReduceSettings settings = new
		 * MapReduceSettings.Builder().setWorkerQueueName("mrworker")
		 * .setWorkerQueueName("mrcontroller").build();
		 * 
		 * MapReduceSpecification mrs = new MapReduceSpecification.Builder( new
		 * DatastoreInput("Account", 50), new CleanAccountsMapper(),
		 * NoReducer.<Void, Void, Void> create(), new NoOutput<Void,
		 * Void>()).build(); String jobId = MapReduceJob.start(mrs, settings);
		 */

		// jobId is used to monitor, see step 5) below
		return new MessagePage("Job ID: "/* + jobId */);
	}
}
