package com.googlecode.npackdweb;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.HTMLWriter;

/**
 * Information about registered actions
 */
public class InfoPage extends MyPage {
    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        HTMLWriter w = new HTMLWriter();

        DefaultServlet ds = DefaultServlet.getInstance(request);
        List<Pattern> ups = ds.getUrlPatterns();
        List<Action> as = ds.getActions();
        w.start("ul");
        for (int i = 0; i < ups.size(); i++) {
            Pattern p = ups.get(i);
            Action a = as.get(i);
            w.e("li", p.pattern() + " -> " + a.getClass().getName());
        }
        w.end("ul");
        return w.toString();
    }

    @Override
    public String getTitle() {
        return "Registered actions";
    }
}
