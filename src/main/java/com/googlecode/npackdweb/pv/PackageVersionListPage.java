package com.googlecode.npackdweb.pv;

import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * List of package versions that were not yet reviewed.
 */
public class PackageVersionListPage extends MyPage {

    private String tag;
    private final String order;

    /**
     * -
     *
     * @param tag a tag to filter package versions or null
     * @param order how to order the query (e.g. "-lastModifiedAt") or null
     */
    public PackageVersionListPage(String tag, String order) {
        this.tag = tag;
        this.order = order;
    }

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        HTMLWriter b = new HTMLWriter();

        String msg = "Package versions";
        if (tag != null) {
            msg += " with the tag " + tag;
        }
        msg += " (showing only the first 20):";
        b.t(msg);
        b.start("ul");
        List<PackageVersion> pvs =
                NWUtils.dsCache.find20PackageVersions(tag, order);
        for (int i = 0; i < pvs.size(); i++) {
            PackageVersion pv = pvs.get(i);

            b.start("li");
            b.e("a", "href", "/p/" + pv.package_ + "/" + pv.version,
                    pv.package_ + " " + pv.version);
            b.t(" installation failed: " + pv.installFailed + " times");
            b.end("li");
        }
        b.end("ul");

        return b.toString();
    }

    @Override
    public String getTitle() {
        return "Package versions";
    }
}
