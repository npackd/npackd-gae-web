package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * XML for a repository.
 */
public class RepXMLPage extends Page {
	private Repository r;

	/**
	 * @param r
	 *            a repository
	 */
	public RepXMLPage(Repository r) {
		this.r = r;
	}

	@Override
	public void create(HttpServletRequest request, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("application/xml");

		try {
			Document d = r.toXML();
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.transform(new DOMSource(d.getDocumentElement()),
					new StreamResult(resp.getOutputStream()));
			resp.getOutputStream().close();
		} catch (Exception e) {
			throw (IOException) new IOException(e.getMessage()).initCause(e);
		}
	}
}
