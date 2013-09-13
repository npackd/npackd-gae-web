package com.googlecode.npackdweb;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.objectify.Objectify;

/**
 * List of package versions that were not yet reviewed.
 */
public class NotReviewedPage extends MyPage {
    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        HTMLWriter b = new HTMLWriter();

        b.t("These package versions were not yet reviewed and may be unsafe (showing only the first 20):");
        b.start("ul");
        Objectify ofy = DefaultServlet.getObjectify();
        List<PackageVersion> pvs = PackageVersion.find20NotReviewed(ofy);
        for (int i = 0; i < pvs.size(); i++) {
            PackageVersion pv = pvs.get(i);

            b.start("li");
            b.e("a", "href", "/p/" + pv.package_ + "/" + pv.version,
                    pv.package_ + " " + pv.version);
            b.end("li");
        }
        b.end("ul");

        return b.toString();
    }

    @Override
    public String getTitle() {
        return "Not reviewed";
    }
}
