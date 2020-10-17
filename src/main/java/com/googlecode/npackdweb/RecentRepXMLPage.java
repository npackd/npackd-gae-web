package com.googlecode.npackdweb;

import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Page;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Document;

public class RecentRepXMLPage extends Page {

    private String user;
    private String tag;

    /**
     * @param user email address or null
     * @param tag tag for package versions to filter or null
     */
    public RecentRepXMLPage(String user, String tag) {
        this.user = user;
        this.tag = tag;
    }

    @Override
    public void create(HttpServletRequest request, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/xml");

        byte[] value;
        try {
            List<PackageVersion> res = NWUtils.dsCache.
                    getRecentlyChangedPackageVersions();

            Document d = RepXMLPage.toXML(res, false, null);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NWUtils.serializeXML(d, baos);
            value = baos.toByteArray();
        } catch (Exception e) {
            throw new IOException(e);
        }

        ServletOutputStream ros = resp.getOutputStream();
        ros.write(value);
        ros.close();
    }
}
