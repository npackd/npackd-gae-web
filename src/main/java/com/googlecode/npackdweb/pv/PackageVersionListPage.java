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
    private static final int PAGE_SIZE = 20;

    private final int offset;
    private String tag;

    /**
     * -
     *
     * @param tag a tag to filter package versions or null
     * @param offset offset in the results
     */
    public PackageVersionListPage(String tag, int offset) {
        this.tag = tag;
        this.offset = offset;
    }

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        HTMLWriter b = new HTMLWriter();

        String msg = "Package versions";
        if (tag != null) {
            msg += " with the tag " + tag;
        }
        msg += " :";
        b.t(msg);
        b.start("ul");
        List<PackageVersion> pvs =
                NWUtils.dsCache.findPackageVersions(tag, null, PAGE_SIZE + 1, offset);
        for (int i = 0; i < Math.min(pvs.size(), PAGE_SIZE); i++) {
            PackageVersion pv = pvs.get(i);

            b.start("li");
            b.e("a", "href", "/p/" + pv.package_ + "/" + pv.version,
                    pv.package_ + " " + pv.version);
            b.t(" installation failed: " + pv.installFailed + " times");
            b.end("li");
        }
        b.end("ul");

        b.unencoded(createPager(offset, pvs.size() > PAGE_SIZE));

        return b.toString();
    }

    @Override
    public String getTitle() {
        return "Package versions";
    }

    private String createPager(int cur, boolean hasNextPage) {
        HTMLWriter w = new HTMLWriter();
        w.start("ul", "class", "pager");
        if (cur >= PAGE_SIZE) {
            w.start("li");
            w.e("a", "href", "/pv?start=" + (cur - PAGE_SIZE) + "&tag=" +
                    NWUtils.encode(tag),
                    "\u2190 Previous page");
            w.end("li");
        } else {
            w.start("li", "class", "disabled");
            w.e("a", "href", "#", "\u2190 Previous page");
            w.end("li");
        }

        if (hasNextPage) {
            w.start("li");
            w.e("a", "href", "/pv?start=" + (cur + PAGE_SIZE) + "&tag=" +
                            NWUtils.encode(tag),
                    "Next page \u2192");
            w.end("li");
        } else {
            w.start("li", "class", hasNextPage ? null : "disabled");
            w.e("a", "href", "#", "Next page \u2192");
            w.end("li");
        }

        w.end("ul");
        return w.toString();
    }
}
