package com.googlecode.npackdweb.api;

import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.wlib.Page;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

        Document d = NWUtils.newXML();
        Element package_ = p.toXML(d, false);
        d.appendChild(package_);

        resp.setContentType("application/xml");
        ServletOutputStream os = resp.getOutputStream();
        NWUtils.serializeXML(d, os);
        os.close();
    }
}
