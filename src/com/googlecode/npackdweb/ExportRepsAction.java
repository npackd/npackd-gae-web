package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.Page;

public class ExportRepsAction extends Action {
	public ExportRepsAction() {
		super("^/cron/export-reps$");
	}

	@Override
	public Page perform(HttpServletRequest req, HttpServletResponse resp)
	        throws IOException {
		export("stable");
		export("stable64");
		export("libs");
		export("unstable");
		resp.setStatus(200);
		return null;
	}

	private void export(String string) {

	}
}
