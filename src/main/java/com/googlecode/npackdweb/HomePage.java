package com.googlecode.npackdweb;

import com.googlecode.npackdweb.wlib.HTMLWriter;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

/**
 * Home page.
 */
public class HomePage extends MyPage {

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        HTMLWriter w = new HTMLWriter();

		// w.unencoded(PackagesPage.createSearchForm("", false));
        w.unencoded(NWUtils.tmpl("Carousel.html"));

        return w.toString();
    }

    @Override
    public String getTitle() {
        return "Npackd";
    }
}
