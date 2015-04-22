package org.apache.maven.plugins.enforcer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.testing.ArtifactStubFactory;
import org.apache.maven.project.MavenProject;

/**
 * Tests {@link RequireSameVersions}.
 * <p>
 * This test coverage is small yet.
 * 
 * @author <a href="mailto:rwoo@gmx.de">Rod</a>
 */
public class TestRequireSameVersions extends TestCase {

    private MavenProject project = new MavenProject();

    private EnforcerRuleHelper helper;

    private RequireSameVersions rule = new RequireSameVersions();

    /**
     * Some artifacts to play with.
     */
    private Set<Artifact> artefacts = new HashSet<Artifact>();

    @Override
    public void setUp() throws Exception {

        project.setDependencyArtifacts(Collections.emptySet());
        project.setPluginArtifacts(Collections.emptySet());
        project.setReportArtifacts(Collections.emptySet());

        helper = EnforcerTestUtils.getHelper(project);

        ArtifactStubFactory factory = new ArtifactStubFactory();
        artefacts.add(factory.createArtifact("gr.oup", "art", "1.1"));
        artefacts.add(factory.createArtifact("gr.oup", "apt", "1.1"));
        artefacts.add(factory.createArtifact("gr.oup", "rat", "1.2"));
    }

    public void testSetDependencies() throws Exception {

        // set artifacts
        {
            project.setDependencyArtifacts(artefacts);
            assertNull(execute(rule, helper));
        }

        // select two artifacts
        {
            rule.dependencies = Collections.singleton("gr.oup:a");
            String error = execute(rule, helper);
            assertNull(error);
        }

        // select three artifacts
        {
            rule.dependencies = Collections.singleton("gr.oup");
            String error = execute(rule, helper);
            assertNotNull(error);
        }
    }

    public void testSetBuildPlugins() throws Exception {

        // no artifacts, no patterns
        {
            assertNull(execute(rule, helper));
        }

        // set artifacts
        {
            project.setPluginArtifacts(artefacts);
            assertNull(execute(rule, helper));
        }

        // select two artifacts
        {
            rule.buildPlugins = Collections.singleton("gr.oup:a");
            String error = execute(rule, helper);
            assertNull(error);
        }

        // select three artifacts
        {
            rule.buildPlugins = Collections.singleton("gr.oup");
            String error = execute(rule, helper);
            assertNotNull(error);
        }
    }

    public void testSetReportPlugins() throws Exception {

        // set artifacts
        {
            project.setReportArtifacts(artefacts);
            assertNull(execute(rule, helper));
        }

        // select two artifacts
        {
            rule.reportPlugins = Collections.singleton("gr.oup:a");
            String error = execute(rule, helper);
            assertNull(error);
        }

        // select three artifacts
        {
            rule.reportPlugins = Collections.singleton("gr.oup");
            String error = execute(rule, helper);
            assertNotNull(error);
            assertContains(error, "Entries with version 1.1");
            assertContains(error, "gr.oup:art:jar");
            assertContains(error, "gr.oup:apt:jar");
            assertContains(error, "Entries with version 1.2");
            assertContains(error, "gr.oup:rat:jar");
        }
    }

    private void assertContains(String str, String substring) {
        assertTrue("[" + str + "] does not contain [" + substring + "]", str.contains(substring));
    }

    /**
     * Simple wrapper to execute the rule.
     * 
     * @param rule
     * @param helper
     * @return Returns null if an {@link EnforcerRuleException} was thrown by the rule. Returns null otherwise.
     */
    private String execute(RequireSameVersions rule, EnforcerRuleHelper helper) {
        try {
            rule.execute(helper);
            return null;
        } catch (EnforcerRuleException e) {
            assertNotNull(e.getMessage());
            return e.getMessage();
        }
    }
}