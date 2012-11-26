package org.jenkinsci.plugins.database.mysql;

import com.mysql.jdbc.Driver;
import groovy.lang.GroovyShell;
import hudson.Extension;
import hudson.Util;
import hudson.util.FormValidation;
import hudson.util.Secret;
import org.jenkinsci.plugins.database.BasicDataSource2;
import org.jenkinsci.plugins.database.Database;
import org.jenkinsci.plugins.database.DatabaseDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class MySQLDatabase extends Database {
    /**
     * Host name + optional port (in the "host[:port]" format)
     */
    public final String hostname;
    public final String database;
    public final String username;
    public final Secret password;

    public final String properties;

    private transient DataSource source;

    @DataBoundConstructor
    public MySQLDatabase(String hostname, String database, String username, Secret password, String properties) {
        this.hostname = hostname;
        this.database = database;
        this.username = username;
        this.password = password;
        this.properties = properties;
    }

    @Override
    public synchronized DataSource getDataSource() throws SQLException {
        if (source==null) {
            BasicDataSource2 fac = new BasicDataSource2();
            fac.setDriverClass(Driver.class);
            fac.setUrl("jdbc:mysql://" + hostname + '/' + database);
            fac.setUsername(username);
            fac.setPassword(Secret.toString(password));

            try {
                for (Map.Entry e : Util.loadProperties(Util.fixNull(properties)).entrySet()) {
                    fac.addConnectionProperty(e.getKey().toString(), e.getValue().toString());
                }
            } catch (IOException e) {
                throw new SQLException("Invalid properties",e);
            }

            source = fac.createDataSource();
        }
        return source;
    }

    @Extension
    public static class DescriptorImpl extends DatabaseDescriptor {

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
                    validPropertyNames = (Set<String>)gs.evaluate(new InputStreamReader(MySQLDatabase.class.getResourceAsStream("validate.groovy")));
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

        public FormValidation doValidate(
                @QueryParameter String hostname,
                @QueryParameter String database,
                @QueryParameter String username,
                @QueryParameter String password,
                @QueryParameter String properties) {

            try {
                DataSource ds = new MySQLDatabase(hostname, database, username, Secret.fromString(password), properties).getDataSource();
                Connection con = ds.getConnection();
                con.createStatement().execute("SELECT 1");
                con.close();
                return FormValidation.ok("OK");
            } catch (SQLException e) {
                return FormValidation.error(e,"Failed to connect to MySQL");
            }

        }
    }
}
