package org.jenkinsci.plugins.database.mysql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import hudson.util.Secret;
import java.io.IOException;
import org.jenkinsci.plugins.database.GlobalDatabaseConfiguration;
import org.jenkinsci.plugins.database.mysql.MySQLDatabase;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@WithJenkins
@Testcontainers
public class MySQLDatabaseTest {

    public static final String TEST_IMAGE = "mysql:8.2.0";

    @Container
    private static final MySQLContainer<?> mysql = new MySQLContainer<>(TEST_IMAGE);

    public void setConfiguration() throws IOException {
        MySQLDatabase database = new MySQLDatabase(
                mysql.getHost() + ":" + mysql.getMappedPort(3306),
                mysql.getDatabaseName(),
                mysql.getUsername(),
                Secret.fromString(mysql.getPassword()),
                null);
        database.setValidationQuery("SELECT 1");
        GlobalDatabaseConfiguration.get().setDatabase(database);
    }

    @Test
    public void shouldSetConfiguration(JenkinsRule j) throws IOException {
        setConfiguration();
        assertThat(GlobalDatabaseConfiguration.get().getDatabase(), instanceOf(MySQLDatabase.class));
    }

    @Test
    public void shouldConstructDatabase(JenkinsRule j) throws IOException {
        MySQLDatabase database = new MySQLDatabase(
                mysql.getHost() + ":" + mysql.getMappedPort(3306),
                mysql.getDatabaseName(),
                mysql.getUsername(),
                Secret.fromString(mysql.getPassword()),
                null);
        assertThat(database.getDescriptor().getDisplayName(), is("MySQL"));
        assertThat(
                database.getJdbcUrl(),
                is("jdbc:mysql://" + mysql.getHost() + ":" + mysql.getMappedPort(3306) + "/"
                        + mysql.getDatabaseName() + ""));
        assertThat(database.getDriverClass(), is(com.mysql.cj.jdbc.Driver.class));
    }
}
