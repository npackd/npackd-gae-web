package com.googlecode.npackdweb.package_;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.FormMode;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;

/**
 * Create a new package.
 */
public class PackageNewAction extends Action {
    /**
     * -
     */
    public PackageNewAction() {
        super("^/package/new$", ActionSecurityType.EDITOR);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        return new PackageDetailPage(FormMode.CREATE);
    }
}
