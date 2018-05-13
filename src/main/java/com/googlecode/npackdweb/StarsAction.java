package com.googlecode.npackdweb;

import com.googlecode.npackdweb.package_.PackagesPage;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.db.Editor;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Your stars
 */
public class StarsAction extends Action {

    /**
     * -
     */
    public StarsAction() {
        super("^/stars", ActionSecurityType.LOGGED_IN);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        User u = UserServiceFactory.getUserService().getCurrentUser();
        Editor e = NWUtils.dsCache.findEditor(u);
        List<String> starredPackages = new ArrayList<>();
        if (e != null) {
            starredPackages.addAll(e.starredPackages);
        }

        PackagesPage p = new PackagesPage(starredPackages);
        p.showSearch = false;
        return p;
    }
}
