package com.googlecode.npackdweb.pv;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.NWUtils.Info;
import com.googlecode.npackdweb.PackageVersion;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Compute SHA1 for a package version.
 */
public class PackageVersionComputeSHA1Action extends Action {
    /**
     * -
     */
    public PackageVersionComputeSHA1Action() {
        super("^/package-version/compute-sha1$", ActionSecurityType.EDITOR);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String package_ = req.getParameter("package");
        String version = req.getParameter("version");
        Objectify ofy = NWUtils.getObjectify();
        PackageVersion p = ofy.get(new Key<PackageVersion>(
                PackageVersion.class, package_ + "@" + version));
        Page ret = null;
        try {
            Info info = NWUtils.download(p.url);
            p.sha1 = NWUtils.byteArrayToHexString(info.sha1);
            NWUtils.savePackageVersion(ofy, p);
            ret = new PackageVersionPage(p, false);
        } catch (IOException e) {
            ret = new MessagePage("Cannot download the file: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            NWUtils.throwInternal(e);
        }
        return ret;
    }
}
