package com.googlecode.npackdweb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.npackdweb.package_.PackageDeleteAction;
import com.googlecode.npackdweb.package_.PackageDeleteConfirmedAction;
import com.googlecode.npackdweb.package_.PackageDetailAction;
import com.googlecode.npackdweb.package_.PackageNewAction;
import com.googlecode.npackdweb.package_.PackageSaveAction;
import com.googlecode.npackdweb.pv.CopyPackageVersionAction;
import com.googlecode.npackdweb.pv.CopyPackageVersionConfirmedAction;
import com.googlecode.npackdweb.pv.DetectPackageVersionAction;
import com.googlecode.npackdweb.pv.DontCheckDownloadAction;
import com.googlecode.npackdweb.pv.PackageVersionComputeSHA1Action;
import com.googlecode.npackdweb.pv.PackageVersionDeleteAction;
import com.googlecode.npackdweb.pv.PackageVersionDeleteConfirmedAction;
import com.googlecode.npackdweb.pv.PackageVersionDetailAction;
import com.googlecode.npackdweb.pv.PackageVersionNewAction;
import com.googlecode.npackdweb.pv.PackageVersionSaveAction;
import com.googlecode.npackdweb.wlib.Action;
import com.googlecode.npackdweb.wlib.Page;
import com.googlecode.npackdweb.wlib.SendStatusAction;

/**
 * Default servlet for HTML pages.
 */
@SuppressWarnings("serial")
public class DefaultServlet extends HttpServlet {
    /** version of the data (versions, packages, licenses): 0, 1, ... */
    public static AtomicInteger dataVersion = new AtomicInteger();

    private List<Pattern> urlPatterns = new ArrayList<Pattern>();
    private List<Action> actions = new ArrayList<Action>();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        req.setAttribute("com.googlecode.npackdweb.DefaultServlet", this);

        String pi = req.getRequestURI();
        // NWUtils.LOG.severe("getPathInfo(): " + pi);
        if (pi == null) {
            pi = "/";
        }

        Action found = null;
        for (int i = 0; i < getUrlPatterns().size(); i++) {
            Pattern p = getUrlPatterns().get(i);
            Matcher m = p.matcher(pi);
            if (m.matches()) {
                found = getActions().get(i);
                break;
            }
        }

        if (found != null) {
            UserService us = UserServiceFactory.getUserService();
            boolean ok = true;
            switch (found.getSecurityType()) {
            case ANONYMOUS:
                break;
            case LOGGED_IN:
                if (us.getCurrentUser() == null) {
                    ok = false;
                    resp.sendRedirect(us.createLoginURL(req.getRequestURI()));
                }
                break;
            case ADMINISTRATOR:
                if (us.getCurrentUser() == null) {
                    ok = false;
                    resp.sendRedirect(us.createLoginURL(req.getRequestURI()));
                } else if (!us.isUserAdmin()) {
                    ok = false;
                    resp.setContentType("text/plain");
                    resp.getWriter().write("Not an admin");
                    resp.getWriter().close();
                }
                break;
            case EDITOR:
                if (us.getCurrentUser() == null) {
                    ok = false;
                    resp.sendRedirect(us.createLoginURL(req.getRequestURI()));
                } else if (!NWUtils.isEditorLoggedIn()) {
                    ok = false;
                    resp.setContentType("text/plain");
                    resp.getWriter()
                            .write("Not an editor. "
                                    + "Please file an issue at http://code.google.com/p/"
                                    + "windows-package-manager/issues/entry "
                                    + "to become one.");
                    resp.getWriter().close();
                }
                break;
            default:
                throw new InternalError("Unknown security type");
            }

            if (ok) {
                Page p = found.perform(req, resp);
                if (p != null) {
                    p.create(req, resp);
                }
            }
        } else {
            resp.sendError(404, "Unknown command: " + pi);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        NWUtils.initObjectify();
        NWUtils.initFreeMarker(getServletContext());

        /* repository */
        registerAction(new RepDeleteAction());
        registerAction(new RepAddAction());
        registerAction(new RepUploadAction());
        registerAction(new RepFromFileAction());
        registerAction(new RepDetailAction());
        registerAction(new RepAction());
        registerAction(new RepXMLAction());
        registerAction(new RecentRepXMLAction());

        /* package */
        registerAction(new PackagesAction());
        registerAction(new PackageDetailAction());
        registerAction(new PackageNewAction());
        registerAction(new PackageSaveAction());
        registerAction(new PackageDeleteAction());
        registerAction(new PackageDeleteConfirmedAction());

        /* package version */
        registerAction(new PackageVersionDetailAction());
        registerAction(new PackageVersionNewAction());
        registerAction(new PackageVersionSaveAction());
        registerAction(new PackageVersionDeleteAction());
        registerAction(new PackageVersionDeleteConfirmedAction());
        registerAction(new CopyPackageVersionAction());
        registerAction(new CopyPackageVersionConfirmedAction());
        registerAction(new EditAsXMLAction());
        registerAction(new DetectPackageVersionAction());
        registerAction(new PackageVersionComputeSHA1Action());
        registerAction(new DontCheckDownloadAction());

        registerAction(new HomeAction());
        registerAction(new SendStatusAction("^/robots\\.txt$", 404));
        registerAction(new CheckDownloadAction());
        registerAction(new CheckUpdatesAction());
        registerAction(new SendStatusAction("^/cron/tick$", 200));
        registerAction(new ExportRepsAction());
        // registerAction(new StoreDataAction());
        registerAction(new RecreateIndexAction());
        registerAction(new ResavePackagesAction());
        registerAction(new AddEditorAction());
        registerAction(new AddEditorConfirmedAction());
        registerAction(new InfoAction());
        registerAction(new DownloadFailedAction());
    }

    /**
     * Registers an action
     * 
     * @param action
     *            registered action
     */
    private void registerAction(Action action) {
        getUrlPatterns().add(Pattern.compile(action.getURLRegExp()));
        getActions().add(action);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    /**
     * @return list of registered URL patterns
     */
    public List<Pattern> getUrlPatterns() {
        return urlPatterns;
    }

    /**
     * @return list of registered actions
     */
    public List<Action> getActions() {
        return actions;
    }
}