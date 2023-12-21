package com.googlecode.npackdweb.pv;

import com.googlecode.npackdweb.MyPage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.HTMLWriter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * List of package versions that were not yet reviewed.
 */
public class PackageVersionListPage extends MyPage {
    private static final int PAGE_SIZE = 20;

    private final int offset;
    private final String tag;

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
                NWUtils.dsCache.findPackageVersions(tag, null, PAGE_SIZE + 1,
                        offset);
        for (int i = 0; i < Math.min(pvs.size(), PAGE_SIZE); i++) {
            PackageVersion pv = pvs.get(i);

            b.start("li");
            b.e("a", "href", "/p/" + pv.package_ + "/" + pv.version,
                    pv.package_ + " " + pv.version);
            b.t(" last modified by ");
            b.unencoded(pv.lastModifiedBy == null ? "" : NWUtils.obfuscateEmail(
                    pv.lastModifiedBy.getEmail(), request.getServerName()));
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
            String url = "/pv?start=" + (cur - PAGE_SIZE);
            if (tag != null) {
                url += "&tag=" + NWUtils.encode(tag);
            }
            w.e("a", "href", url,
                    "← Previous page");
            w.end("li");
        } else {
            w.start("li", "class", "disabled");
            w.e("a", "href", "#", "← Previous page");
            w.end("li");
        }

        if (hasNextPage) {
            w.start("li");
            String url = "/pv?start=" + (cur + PAGE_SIZE);
            if (tag != null) {
                url += "&tag=" + NWUtils.encode(tag);
            }
            w.e("a", "href", url, "Next page →");
            w.end("li");
        } else {
            w.start("li", "class", "disabled");
            w.e("a", "href", "#", "Next page →");
            w.end("li");
        }

        w.end("ul");
        return w.toString();
    }
}
