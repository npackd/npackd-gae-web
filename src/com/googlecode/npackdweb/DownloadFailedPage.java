package com.googlecode.npackdweb;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.objectify.Objectify;

/**
 * List of package versions with errors in downloading the binary.
 */
public class DownloadFailedPage extends MyPage {
    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        HTMLWriter b = new HTMLWriter();

        b.t("The download check failed for these package versions (showing only the first 20):");
        b.start("ul");
        Objectify ofy = NWUtils.getObjectify();
        List<PackageVersion> pvs = PackageVersion.find20DownloadFailed(ofy);
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
        return "Failed downloads";
    }
}
