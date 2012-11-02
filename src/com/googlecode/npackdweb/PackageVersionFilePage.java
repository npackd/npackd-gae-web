package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.wlib.HTMLWriter;

/**
 * Edit package version file.
 */
public class PackageVersionFilePage extends MyPage {
	private PackageVersion pv;
	private String path;

	/**
	 * @param pv
	 *            a package version
	 * @param path
	 *            <file path="">
	 */
	public PackageVersionFilePage(PackageVersion pv, String path) {
		this.pv = pv;
		this.path = path;
	}

	@Override
	public String createContent(HttpServletRequest request) throws IOException {
		int index = this.pv.filePaths.indexOf(this.path);
		String content = "";
		if (index >= 0) {
			content = pv.getFileContents(index);
		}

		HTMLWriter w = new HTMLWriter();
		w.start("form", "method", "post", "action", "/pv-file/save");
		w.e("input", "type", "hidden", "name", "name", "value", pv.name);
		w.e("input", "type", "hidden", "name", "path", "value", path);
		w.start("table");

		w.start("tr");
		w.e("td", "File path:");
		w.start("td");
		w.t(path);
		w.end("td");
		w.end("tr");

		w.start("tr");
		w.e("td", "File content:");
		w.start("td");
		w.e("textarea", "name", "content", "rows", "20", "cols", "80", "wrap",
				"off", content);
		w.end("td");
		w.end("tr");

		w.end("table");
		w.e("input", "class", "input", "type", "submit", "value", "Save");
		w.end("form");

		return w.toString();
	}

	@Override
	public String getTitle() {
		return "File";
	}
}
