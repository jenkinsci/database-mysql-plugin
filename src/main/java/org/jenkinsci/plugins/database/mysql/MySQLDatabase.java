package org.jenkinsci.plugins.database.mysql;

import com.mysql.jdbc.Driver;
import groovy.lang.GroovyShell;
import hudson.Extension;
import hudson.Util;
import hudson.util.FormValidation;
import hudson.util.Secret;
import org.jenkinsci.plugins.database.AbstractRemoteDatabase;
import org.jenkinsci.plugins.database.AbstractRemoteDatabaseDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class MySQLDatabase extends AbstractRemoteDatabase {
    @DataBoundConstructor
    public MySQLDatabase(String hostname, String database, String username, Secret password, String properties) {
        super(hostname, database, username, password, properties);
    }

    @Override
    protected Class<Driver> getDriverClass() {
        return Driver.class;
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:mysql://" + hostname + '/' + database;
    }

    @Extension
    public static class DescriptorImpl extends AbstractRemoteDatabaseDescriptor {

        private volatile Set<String> validPropertyNames;

        @Override
        public String getDisplayName() {
            return "MySQL";
        }

        public FormValidation doCheckProperties(@QueryParameter String properties) throws IOException {
            try {
                if (validPropertyNames==null) {
                    // this computation depends on the implementation details of MySQL JDBC connector
                    GroovyShell gs = new GroovyShell(getClass().getClassLoader());
                    validPropertyNames = (Set<String>) gs.evaluate ( new InputStreamReader ( MySQLDatabase.class.getResourceAsStream ( "validate.groovy" ),
                        Charset.forName ( "UTF-8" ) ) );
                }
                Properties props = Util.loadProperties(properties);
                for (Map.Entry e : props.entrySet()) {
                    String key = e.getKey().toString();
                    if (!validPropertyNames.contains(key))
                        return FormValidation.error("Unrecognized property: "+key);
                }
                return FormValidation.ok();
            } catch (Throwable e) {
                return FormValidation.warning(e,"Failed to validate the connection properties");
            }
        }
    }
}
