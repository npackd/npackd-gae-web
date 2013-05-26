package com.googlecode.npackdweb;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.objectify.Objectify;

/**
 * List of repositories.
 */
public class RepPage extends MyPage {
    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        HTMLWriter b = new HTMLWriter();

        b.t("These repositories are re-created daily:");
        b.start("ul");
        Objectify ofy = NWUtils.getObjectify();
        List<Repository> reps = Repository.findAll(ofy);
        for (int i = 0; i < reps.size(); i++) {
            Repository r = reps.get(i);

            b.start("li");
            b.e("a", "href", "/rep/xml?tag=" + r.name, r.name);
            b.end("li");
        }
        b.end("ul");

        b.e("br");
        b.t("This repository contains 20 last changed package versions and should be used for testing only: ");
        b.e("a", "href", "/rep/recent-xml",
                "20 recently modified package versions");

        return b.toString();
    }

    @Override
    public String getTitle() {
        return "Repositories";
    }
}
