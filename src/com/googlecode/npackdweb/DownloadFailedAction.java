package com.googlecode.npackdweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.Page;

/**
 * Shows the list of package versions with errors in binary downloads.
 */
public class DownloadFailedAction extends Action {
    /**
     * -
     */
    public DownloadFailedAction() {
        super("^/download-failed$");
    }

    @Override
    public Page perform(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        return new DownloadFailedPage();
    }
}
