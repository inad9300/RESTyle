package es.berry.restyle.core;

import es.berry.restyle.specification.SpecObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static junit.framework.TestCase.assertTrue;
import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TemplateGenTest {

    private TemplateGen templateGen = null;
    private File templateGenDir = null;

    @Before
    public void setUp() throws IOException {
        SpecObjectMapper.configure("whatever.json");

        templateGen = new TemplateGen(createMock(Generator.class).getClass());

        // This test must work before any of the rest are even tried
        templateGenDir = testGetDefaultDir(templateGen);
    }

    private File testGetDefaultDir(TemplateGen g) throws IOException {
        final File defaultDir = new File(g.getBaseDir());
        if (!defaultDir.mkdirs())
            throw new IOException("Could not create temp directory: " + defaultDir.getAbsolutePath());

        assertTrue(defaultDir.exists());
        assertTrue(defaultDir.canRead());
        assertTrue(defaultDir.isDirectory());

        return defaultDir;
    }

    @After
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(templateGenDir);
        } catch (IOException e) {
            System.out.println("Could not clean up files after template generation tests");
            e.printStackTrace();
        }
    }

    @Test
    public void compile() {
        final String templateBasename = "__test_template__";
        final String expectedResult = "Hello, world!";
        final String jsonValues = "{\"name\":\"world\"}";

        // We solely test a basic template to check if everything is working fine in integration, but we trust the
        // library we are using does what it is supposed to do
        final String template = "Hello, {{name}}!";

        File tmpFile = new File(templateGenDir.getAbsolutePath() + File.separator + templateBasename + ".hbs");
        File tmpValuesFile;
        try {
            tmpFile.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile));
            bw.write(template);
            bw.close();

            assertEquals(expectedResult, templateGen.compile(templateBasename, jsonValues));
            assertEquals(expectedResult, templateGen.compile(templateBasename,
                    SpecObjectMapper.getInstance().createObjectNode().put("name", "world")));

            tmpValuesFile = File.createTempFile("values", ".tmp", templateGenDir);
            bw = new BufferedWriter(new FileWriter(tmpValuesFile));
            bw.write(jsonValues);
            bw.close();

            assertEquals(expectedResult, templateGen.compile(templateBasename, tmpValuesFile));
        } catch (IOException e) {
            assertFalse("Temporary file creation failed", true);
        }
    }
}
