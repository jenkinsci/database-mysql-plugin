package org.jenkinsci.plugins.database.mysql;

import hudson.Extension;
import hudson.util.Secret;
import org.jenkinsci.plugins.database.AbstractRemoteDatabase;
import org.jenkinsci.plugins.database.AbstractRemoteDatabaseDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Kohsuke Kawaguchi
 */
public class MySQLDatabase extends AbstractRemoteDatabase {
    @DataBoundConstructor
    public MySQLDatabase(String hostname, String database, String username, Secret password, String properties) {
        super(hostname, database, username, password, properties);
    }

    @Override
    protected Class<com.mysql.cj.jdbc.Driver> getDriverClass() {
        return com.mysql.cj.jdbc.Driver.class;
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:mysql://" + hostname + '/' + database;
    }

    @Extension
    public static class DescriptorImpl extends AbstractRemoteDatabaseDescriptor {

        @Override
        public String getDisplayName() {
            return "MySQL";
        }

    }
}
