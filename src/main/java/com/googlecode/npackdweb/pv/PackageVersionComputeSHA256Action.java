package com.googlecode.npackdweb.pv;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.NWUtils.Info;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import static com.googlecode.objectify.ObjectifyService.ofy;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Compute SHA-256 for a package version.
 */
public class PackageVersionComputeSHA256Action extends Action {

    /**
     * -
     */
    public PackageVersionComputeSHA256Action() {
        super("^/package-version/compute-sha-256$",
                ActionSecurityType.LOGGED_IN);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String package_ = req.getParameter("package");
        String version = req.getParameter("version");
        Objectify ofy = ofy();
        Package pa = ofy.load().key(Key.create(Package.class, package_)).now();
        Page page;
        if (!pa.isCurrentUserPermittedToModify()) {
            page =
                    new MessagePage(
                            "You do not have permission to modify this package");
        } else {
            PackageVersion p =
                    ofy.load().key(Key.create(PackageVersion.class,
                                    package_ + "@" + version)).now();
            PackageVersion oldp = p.copy();
            try {
                Info info = p.check(false, "SHA-256");
                p.sha1 = NWUtils.byteArrayToHexString(info.sha1);
                NWUtils.savePackageVersion(ofy, oldp, p, true, true);
                resp.sendRedirect("/p/" + p.package_ + "/" + p.version);
                page = null;
            } catch (IOException e) {
                page =
                        new MessagePage("Cannot download the file: " +
                                e.getMessage());
            }
        }
        return page;
    }
}
