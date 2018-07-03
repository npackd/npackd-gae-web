package com.googlecode.npackdweb.admin;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Repository;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Adds a repository.
 */
public class AddRepositoryConfirmedAction extends Action {

    /**
     * -
     */
    public AddRepositoryConfirmedAction() {
        super("^/add-repository-confirmed$", ActionSecurityType.ADMINISTRATOR);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Page res;

        AddRepositoryPage p = new AddRepositoryPage();
        p.fill(req);
        String err = p.validate();
        if (err == null) {
            Repository r = new Repository();
            r.name = p.tag;
            NWUtils.dsCache.saveRepository(r);
            res = new MessagePage("Repository " + p.tag +
                    " was added successfully");
        } else {
            p.error = err;
            res = p;
        }

        return res;
    }
}
