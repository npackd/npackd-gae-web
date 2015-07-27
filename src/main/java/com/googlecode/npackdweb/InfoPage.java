package com.googlecode.npackdweb;

import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * Information about registered actions
 */
public class InfoPage extends MyPage {

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        HTMLWriter w = new HTMLWriter();

        DefaultServlet ds = DefaultServlet.getInstance(request);
        List<Action> as = ds.getActions();
        w.start("ul");
        for (int i = 0; i < as.size(); i++) {
            Action a = as.get(i);
            w.e("li", a.pattern() + " -> " + a.getClass().getName());
        }
        w.end("ul");
        return w.toString();
    }

    @Override
    public String getTitle() {
        return "Registered actions";
    }
}
