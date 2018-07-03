package com.googlecode.npackdweb.admin;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Adds a repository.
 */
public class AddRepositoryAction extends Action {

    /**
     * -
     */
    public AddRepositoryAction() {
        super("^/add-repository", ActionSecurityType.ADMINISTRATOR);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        return new AddRepositoryPage();
    }
}
