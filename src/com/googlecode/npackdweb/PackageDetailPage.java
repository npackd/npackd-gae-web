package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

public class PackageDetailPage extends FramePage {
	private Package r;

	/**
	 * @param r
	 *            a package or null
	 */
	public PackageDetailPage(Package r) {
		this.r = r;
	}

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		String ret;
		if (r == null) {
			ret = NWUtils.tmpl("PDetail.html", "title", "", "id", "", "name",
					"", "url", "", "description", "");
		} else {
			ret = NWUtils.tmpl("PDetail.html", "title", r.title, "id", r.id
					.toString(), "name", r.name, "url", r.url, "description",
					r.description);
		}
		return ret;
	}

	@Override
	public String getTitle() {
		return "Package";
	}
}
