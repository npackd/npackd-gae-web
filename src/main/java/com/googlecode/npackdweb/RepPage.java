package com.googlecode.npackdweb;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.db.Repository;
import com.googlecode.npackdweb.wlib.HTMLWriter;
import com.googlecode.objectify.Objectify;
import static com.googlecode.objectify.ObjectifyService.ofy;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * List of repositories.
 */
public class RepPage extends MyPage {

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        HTMLWriter b = new HTMLWriter();

        Objectify ofy = ofy();
        List<Repository> reps = Repository.findAll(ofy);
        if (reps.size() > 0) {
            b.
                    t("These repositories are re-created daily, the packages were reviewed and are safe to use:");
            b.start("ul");
            for (int i = 0; i < reps.size(); i++) {
                Repository r = reps.get(i);

                b.start("li");
                b.e("a", "href", "/rep/xml?tag=" + r.name, r.name);
                b.t(" (since Npackd 1.20 also available in ");
                b.e("a", "href", "/rep/zip?tag=" + r.name, "ZIP format");
                b.t(")");
                b.end("li");
            }
            b.end("ul");
        }

        b.e("p", "class", "bg-danger",
                "Packages in the following repositories are not yet reviewed and may be unsafe");

        b.start("ul");
        b.start("li");
        b.
                t("This repository contains 20 last changed package versions and should be used for testing only: ");
        b.e("a", "href", "/rep/recent-xml",
                "20 recently modified package versions");
        b.end("li");

        b.start("li");
        b.
                t("This repository contains 20 last changed package versions marked as untested (the \"untested\" tag) and should be used for testing only: ");
        b.e("a", "href", "/rep/recent-xml?tag=untested",
                "20 recently modified untested package versions");
        b.
                t(". The untested packages will be tested daily at 5 a.m. UTC by an automatic process and marked as tested (the tag \"untested\" will be removed). ");
        b.t("The full log is available at AppVeyor: ");
        b.start("a", "href",
                "https://ci.appveyor.com/project/tim-lebedkov/npackd");
        b.e("img", "src",
                "https://ci.appveyor.com/api/projects/status/lfdudyc18d604lt4?svg=true");
        b.end("a");
        b.end("li");

        User u = UserServiceFactory.getUserService().getCurrentUser();
        if (u != null) {
            b.start("li");
            b.
                    t("This repository contains 20 last changed package versions changed by you and should be used for testing only: ");
            String email = u.getEmail();
            b.e("a", "href", "/rep/recent-xml?user=" + email,
                    "20 recently modified package versions by " + email);
            b.end("li");
        }
        b.end("ul");

        return b.toString();
    }

    @Override
    public String getTitle() {
        return "Repositories";
    }
}
