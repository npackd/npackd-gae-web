package com.googlecode.npackdweb.admin;

import com.google.appengine.api.datastore.Entity;
import com.googlecode.npackdweb.NWUtils;
import com.googlecode.npackdweb.db.Editor;
import com.googlecode.npackdweb.db.Package;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * Deletes inactive users.
 *
 public class DeleteInactiveUsersAction extends Action {

 /**
 * -
 *
 public DeleteInactiveUsersAction() {
 super("^/cron/delete-inactive-users$", ActionSecurityType.ANONYMOUS);
 }

 @Override
 public Page perform(HttpServletRequest req, HttpServletResponse resp)
 throws IOException {
 MapReduceSettings settings =
 new MapReduceSettings.Builder().setWorkerQueueName("default")
 .setBucketName("npackd").build();

 MapSpecification<Entity, Void, Void> ms =
 new MapSpecification.Builder<>(new DatastoreInput(
 "Editor", 10),
 new DeleteInactiveUsersMapper(),
 new NoOutput<Void, Void>()).build();
 String jobId = MapJob.start(ms, settings);

 return new MessagePage("Job ID: " + jobId);
 }
 }
 */
/**
 * Delete inactive users.
 */
public class DeleteInactiveUsersMapper {
    public void map(Entity value) {
        Editor data = new Editor(value);

        NWUtils.LOG.log(Level.INFO, "delete-inactive-users for {0}", data.name);

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

        NWUtils.LOG.log(Level.INFO, "delete-inactive-users days {0}", days);

        if (days > MAX_DAYS) {
            if (data.warnedAboutAccountDeletionDate != null) {
                long sinceWarning = ChronoUnit.DAYS.between(
                        LocalDate.from(data.warnedAboutAccountDeletionDate.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()),
                        LocalDate.now(ZoneId.systemDefault())
                );
                if (sinceWarning > 30) {
                    deleteEditor(data);
                }
            } else {
                String txt = "Hello " + data.name + ", \n\n" +
                        "You have not logged in to https://www.npackd.org for a long time. \n" +
                        "Your data will be deleted in 30 days.\n" +
                        "\n\n" +
                        "--Admin";
                NWUtils.sendMailTo(txt, data.name);
                NWUtils.sendMailToAdmin(txt);
                data.warnedAboutAccountDeletionDate = new Date();
                NWUtils.dsCache.saveEditor(data);
            }
        } else {
            // reset the warning
            if (data.warnedAboutAccountDeletionDate != null) {
                data.warnedAboutAccountDeletionDate = null;
                NWUtils.dsCache.saveEditor(data);
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
            NWUtils.dsCache.savePackage(old, p, true);
        }

        for (String packageName: data.starredPackages) {
            Package p = NWUtils.dsCache.getPackage(packageName, true);
            if (p != null)
                NWUtils.dsCache.starPackage(p, data, false);
        }

        if (packages.size() < 11) {
            NWUtils.dsCache.deleteEditor(data.name);
            String txt = "Hello " + data.name + ", \n\n" +
                    "You have not logged in to https://www.npackd.org for a long time. \n" +
                    "Your data was deleted.\n" +
                    "\n\n" +
                    "--Admin";
            NWUtils.sendMailTo(txt, data.name);
            NWUtils.sendMailToAdmin(txt);
        }
    }
}
