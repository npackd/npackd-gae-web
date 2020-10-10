package com.googlecode.npackdweb;

import com.googlecode.npackdweb.admin.AddRepositoryAction;
import com.googlecode.npackdweb.admin.AddRepositoryConfirmedAction;
import com.googlecode.npackdweb.api.NotifyAction;
import com.googlecode.npackdweb.api.SetURLPackageVersionAction;
import com.googlecode.npackdweb.api.StarAction;
import com.googlecode.npackdweb.api.TagPackageVersionAction;
import com.googlecode.npackdweb.db.Editor;
import com.googlecode.npackdweb.license.LicenseAction;
import com.googlecode.npackdweb.license.LicenseDeleteAction;
import com.googlecode.npackdweb.license.LicenseSaveAction;
import com.googlecode.npackdweb.license.LicensesAction;
import com.googlecode.npackdweb.package_.PackageDeleteConfirmedAction;
import com.googlecode.npackdweb.package_.PackageDetailAction;
import com.googlecode.npackdweb.package_.PackageNewAction;
import com.googlecode.npackdweb.package_.PackageNextAction;
import com.googlecode.npackdweb.package_.PackageRenameConfirmedAction;
import com.googlecode.npackdweb.package_.PackageSaveAction;
import com.googlecode.npackdweb.package_.PackagesAction;
import com.googlecode.npackdweb.package_.RequestPermissionsAction;
import com.googlecode.npackdweb.pv.CopyPackageVersionConfirmedAction;
import com.googlecode.npackdweb.pv.DetectPackageVersionAction;
import com.googlecode.npackdweb.pv.PackageVersionArchiveAction;
import com.googlecode.npackdweb.pv.PackageVersionComputeSHA1Action;
import com.googlecode.npackdweb.pv.PackageVersionComputeSHA256Action;
import com.googlecode.npackdweb.pv.PackageVersionDeleteConfirmedAction;
import com.googlecode.npackdweb.pv.PackageVersionDetailAction;
import com.googlecode.npackdweb.pv.PackageVersionListAction;
import com.googlecode.npackdweb.pv.PackageVersionMarkReviewedAction;
import com.googlecode.npackdweb.pv.PackageVersionNewAction;
import com.googlecode.npackdweb.pv.PackageVersionRecognizeAction;
import com.googlecode.npackdweb.pv.PackageVersionSaveAction;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.npackdweb.wlib.SendRedirectAction;
import com.googlecode.npackdweb.wlib.SendStatusAction;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Default servlet for HTML pages.
 */
@SuppressWarnings("serial")
public class DefaultServlet extends HttpServlet {

    /**
     * @param req an HTTP request
     * @return DefaultServlet instance
     */
    public static DefaultServlet getInstance(HttpServletRequest req) {
        return (DefaultServlet) req
                .getAttribute("com.googlecode.npackdweb.DefaultServlet");
    }

    private List<Action> actions = new ArrayList<>();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        doGet0(req, resp);
    }

    private void doGet0(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        req.setAttribute("com.googlecode.npackdweb.DefaultServlet", this);

        String pi = req.getRequestURI();
        // NWUtils.LOG.severe("getPathInfo(): " + pi);
        if (pi == null) {
            pi = "/";
        }
        System.out.println("öasldfkjöaslfjölasdkfjlöasdkfjösladkfjö l " + pi);

        Action found = null;
        for (int i = 0; i < actions.size(); i++) {
            Action p = actions.get(i);
            if (p.matches(req)) {
                found = p;
                break;
            }
        }

        if (found != null) {
            AuthService us = AuthService.getInstance();
            final User currentUser = us.getCurrentUser();
            if (currentUser != null) {
                Editor e = NWUtils.dsCache.findEditor(currentUser);
                if (e == null) {
                    e = new Editor(currentUser);
                    NWUtils.dsCache.saveEditor(e);
                } else {
                    Date when = NWUtils.newDay();
                    if (e.lastLogin == null || e.lastLogin.before(when)) {
                        e.lastLogin = when;
                        NWUtils.dsCache.saveEditor(e);
                    }
                }
            }
            boolean ok = true;
            switch (found.getSecurityType()) {
                case ANONYMOUS:
                    break;
                case LOGGED_IN:
                    if (currentUser == null) {
                        ok = false;
                        NWUtils.LOG.info("Calling createLoginURL");
                        resp.
                                sendRedirect(us.createLoginURL(req.
                                        getRequestURI()));
                    }
                    break;
                case ADMINISTRATOR:
                    if (currentUser == null) {
                        ok = false;
                        NWUtils.LOG.info("Calling createLoginURL");
                        resp.
                                sendRedirect(us.createLoginURL(req.
                                        getRequestURI()));
                    } else if (!us.isUserAdmin()) {
                        ok = false;
                        resp.setContentType("text/plain");
                        resp.getWriter().write("Not an admin");
                        resp.getWriter().close();
                    }
                    break;
                default:
                    throw new InternalError("Unknown security type");
            }

            if (ok) {
                NWUtils.LOG.info("Before perform");
                Page p = found.perform(req, resp);
                if (p != null) {
                    p.create(req, resp);
                }
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Unknown command: " + pi);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        NWUtils.initFreeMarker(getServletContext());

        registerAction(new SendRedirectAction("^(/.+)/$", "%2$s"));

        /* repository */
        registerAction(new RepDeleteAction());
        registerAction(new RepAddAction());
        registerAction(new RepUploadAction());
        registerAction(new RepFromFileAction());
        registerAction(new RepDetailAction());
        registerAction(new RepAction());
        registerAction(new RepXMLAction());
        registerAction(new RepZIPAction());
        registerAction(new RecentRepXMLAction());

        /* package */
        registerAction(new PackagesAction());
        registerAction(new PackageDetailAction());
        registerAction(new PackageNewAction());
        registerAction(new PackageSaveAction());
        registerAction(new PackageDeleteConfirmedAction());
        registerAction(new PackageNextAction());
        registerAction(new RequestPermissionsAction());
        registerAction(new PackageRenameConfirmedAction());

        /* package version */
        registerAction(new PackageVersionDetailAction());
        registerAction(new PackageVersionNewAction());
        registerAction(new PackageVersionSaveAction());
        registerAction(new PackageVersionDeleteConfirmedAction());
        registerAction(new CopyPackageVersionConfirmedAction());
        registerAction(new EditAsXMLAction());
        registerAction(new DetectPackageVersionAction());
        registerAction(new PackageVersionComputeSHA1Action());
        registerAction(new PackageVersionComputeSHA256Action());
        registerAction(new PackageVersionRecognizeAction());
        registerAction(new PackageVersionMarkReviewedAction());
        registerAction(new PackageVersionArchiveAction());

        /* license */
        registerAction(new LicensesAction());
        registerAction(new LicenseAction());
        registerAction(new LicenseDeleteAction());
        registerAction(new LicenseSaveAction());

        registerAction(new PackageVersionListAction());

        registerAction(new HomeAction());
        registerAction(new StarsAction());
        registerAction(new SendStatusAction("^/robots\\.txt$", 404));
        registerAction(new SendStatusAction("^/cron/tick$", 200));
        registerAction(new ExportRepsAction());
        // registerAction(new StoreDataAction());
        registerAction(new AddRepositoryAction());
        registerAction(new AddRepositoryConfirmedAction());
        registerAction(new InfoAction());
        registerAction(new StarFragmentAction());
        registerAction(new ReCaptchaAnswerAction());
        registerAction(new ReCaptchaAction());

        /* API */
        registerAction(new TagPackageVersionAction());
        registerAction(new NotifyAction());
        registerAction(new StarAction());
        registerAction(new SetURLPackageVersionAction());
    }

    /**
     * Registers an action
     *
     * @param action registered action
     */
    private void registerAction(Action action) {
        getActions().add(action);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    /**
     * @return list of registered actions
     */
    public List<Action> getActions() {
        return actions;
    }
}
