package com.googlecode.npackdweb.wlib;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * An HTML page.
 */
public abstract class Page {
    /**
     * Creates the content of a page
     *
     * @param request HTTP request
     * @param resp HTTP response
     * @throws IOException any error
     */
    public abstract void create(HttpServletRequest request,
                                HttpServletResponse resp) throws IOException;
}
