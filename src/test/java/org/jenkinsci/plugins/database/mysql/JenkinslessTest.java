package org.jenkinsci.plugins.database.mysql;

import hudson.util.FormValidation.Kind;
import org.jenkinsci.plugins.database.mysql.MySQLDatabase.DescriptorImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Kohsuke Kawaguchi
 */
public class JenkinslessTest {
    @Test
    public void testPropertyValidation() throws Exception {
        DescriptorImpl d = new DescriptorImpl();
        Assert.assertEquals(Kind.OK,d.doCheckProperties("autoReconnect=true").kind);
        Assert.assertEquals(Kind.OK,d.doCheckProperties("").kind);

        Assert.assertEquals(Kind.ERROR,d.doCheckProperties("bogus=true").kind);
    }
}
