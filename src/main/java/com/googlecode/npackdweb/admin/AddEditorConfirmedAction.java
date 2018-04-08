package com.googlecode.npackdweb.admin;

import com.googlecode.npackdweb.MessagePage;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Editor;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.ActionSecurityType;
import com.googlecode.npackdweb.wlib.Page;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Adds an editor.
 */
public class AddEditorConfirmedAction extends Action {

    /**
     * -
     */
    public AddEditorConfirmedAction() {
        super("^/add-editor-confirmed$", ActionSecurityType.ADMINISTRATOR);
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Page res;

        AddEditorPage p = new AddEditorPage();
        p.fill(req);
        String err = p.validate();
        if (err == null) {
            Editor e = new Editor(NWUtils.email2user(p.email));
            NWUtils.dsCache.saveEditor(e);
            res = new MessagePage("Editor " + p.email +
                    " was added successfully");
        } else {
            p.error = err;
            res = p;
        }

        return res;
    }
}
