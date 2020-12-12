package com.googlecode.npackdweb.admin;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Editor;
import com.googlecode.npackdweb.db.Package;
import com.googlecode.npackdweb.db.PackageVersion;
import com.googlecode.npackdweb.db.Version;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * Delete inactive users.
 */
public class DeleteInactiveUsersMapper extends MapOnlyMapper<Entity, Void> {

    private static final long serialVersionUID = 1L;

    private transient DatastoreMutationPool pool;

    @Override
    public void beginSlice() {
        this.pool = DatastoreMutationPool.create();
    }

    @Override
    public void endSlice() {
        this.pool.flush();
    }

    @Override
    public void map(Entity value) {
        Editor data = new Editor(value);

        //NWUtils.LOG.log(Level.INFO, "delete-inactive-users for {0}", data.name);

        Date v = data.lastLogin;
        if (v == null)
            v = data.lastModifiedAt;
        long days = ChronoUnit.DAYS.between(
                LocalDate.from(v.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()),
                LocalDate.now(ZoneId.systemDefault())
        );

        final long MAX_DAYS = 365 * 2;

        if (days > MAX_DAYS) {
            if (!data.warnedAboutAccountDeletion) {
                NWUtils.sendMailToAdmin("Hello " + data.name + ", \n\n" +
                                "You have not logged in to https://www.npackd.org for a long time. \n" +
                                "Your data will be deleted in 30 days.\n" +
                                "\n\n" +
                                "--Admin");
                //data.warnedAboutAccountDeletion = true;
                NWUtils.dsCache.saveEditor(data);
            } else if (days > MAX_DAYS + 30) {
                deleteEditor(data);
            }
        }
    }

    private void deleteEditor(Editor data) {
        final List<Package> packages =
                NWUtils.dsCache.findPackages(null, null, data.name, 11);
        for (Package p: packages) {
            Package old = p.copy();

            NWUtils.LOG.log(Level.INFO, "deleting permission for {0} from {1}",
                    new Object[] {
                    data.name,
                    p.name});

            p.permissions.remove(data.name);
            if (p.permissions.size() == 0)
                p.permissions.add(NWUtils.email2user(NWUtils.THE_EMAIL));
            // TODO: later NWUtils.dsCache.savePackage(old, p, true);
        }

        if (packages.size() < 11) {
            // TODO: later NWUtils.dsCache.deleteEditor(data.name);
            NWUtils.sendMailToAdmin("Hello " + data.name + ", \n\n" +
                            "You have not logged in to https://www.npackd.org for a long time. \n" +
                            "Your data was deleted.\n" +
                            "\n\n" +
                            "--Admin");
        }
    }
}
