package com.googlecode.npackdweb.api;

import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.npackdweb.wlib.Page;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * XML for a package.
 */
public class PackageXMLPage extends Page {

    private final String name;

    /**
     * @param name package name
     */
    public PackageXMLPage(String name) {
        this.name = name;
    }

    @Override
    public void create(HttpServletRequest request, HttpServletResponse resp)
            throws IOException {
        Package p = NWUtils.dsCache.getPackage(name, true);

        HTMLWriter d = new HTMLWriter();
        d.setPretty(true);
        d.documentStart();
        p.toXML(d, true);

        resp.setContentType("application/xml");
        ServletOutputStream os = resp.getOutputStream();
        os.write(d.getContent().toString().getBytes(StandardCharsets.UTF_8));
        os.close();
    }
}
