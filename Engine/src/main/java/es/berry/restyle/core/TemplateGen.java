package es.berry.restyle.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import es.berry.restyle.logging.Log;
import es.berry.restyle.logging.Logger;
import es.berry.restyle.specification.SpecObjectMapper;
import es.berry.restyle.utils.Strings;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Wrapper class for the Handlebars.java library.
 * <p>
 * Documentation at https://jknack.github.io/handlebars.java and https://github.com/jknack/handlebars.java
 */
public class TemplateGen {

    final private String ext;
    final private String baseDir;
    final private Handlebars handlebars;
    final private static String TMPL_DIR_PART = "/templates/";

    private static final Logger log = Log.getChain();

    public TemplateGen(Class c, String ext) {
        this.baseDir = calculateBaseDir(c);
        this.ext = tuneExtension(ext);

        final TemplateLoader loader = new FileTemplateLoader(this.baseDir, this.ext);
        this.handlebars = new Handlebars(loader);
        this.handlebars.registerHelper("json", Jackson2Helper.INSTANCE);
    }

    public TemplateGen(Class c) {
        this(c, null);
    }

    /**
     * Return the directory where the templates are supposed to be by default.
     */
    private static String calculateBaseDir(Class c) {
        String dir = Config.getConfigDir().getAbsolutePath() + TMPL_DIR_PART;

        return c == null ? dir : dir + c.getSimpleName() + "/";
    }

    public String getBaseDir() {
        return this.baseDir;
    }

    /**
     * Ensure that an extension is properly prefixed.
     */
    private static String tuneExtension(String ext) {
        if (Strings.isEmpty(ext))
            return ".hbs"; // Default value for Handlebars.java

        return ext.startsWith(".") ? ext : "." + ext;
    }

//    /**
//     * This method provides a way to get the content of a resource (a template) as a String, given the path pointing to
//     * it. It is necessary due to problems when referring to a file once the application's JAR is generated (using
//     * relative paths and the File interface worked from the IDE, but not from the JAR file).
//     */
//    private static String getResourceTemplate(String resourcePath) throws IOException {
//        final InputStream in = TemplateGen.class.getResourceAsStream(resourcePath);
//        if (in == null)
//            throw new NullPointerException("Stream corresponding to the resource is null: " + resourcePath);
//        return Strings.streamToString(in);
//    }

    /**
     * Check the given template's name to increase the probabilities of its correctness.
     */
    private String normalizeTemplateName(String name) {
//        final String suffix = name.endsWith(this.ext) ? "" : this.ext;
//        name = StringUtils.strip(name, "/");
//        return this.baseDir + name + suffix;
        return StringUtils.strip(name, "/");
    }

    /**
     * Resolve placeholders in a Handlebars template, replacing them with actual values, given as a JsonNode.
     * <p>
     * NOTE: example of ObjectNode (extends JsonNode) creation:
     * ObjectNode node = new ObjectMapper().createObjectNode();
     * node.put("key", "value");
     */
    public String compile(String tmplName, JsonNode jsonNode) {
        try {
            final Template template = handlebars.compile(normalizeTemplateName(tmplName));

            final Context context = Context.newBuilder(jsonNode).resolver(
                    JsonNodeValueResolver.INSTANCE,
                    JavaBeanValueResolver.INSTANCE,
                    FieldValueResolver.INSTANCE,
                    MapValueResolver.INSTANCE,
                    MethodValueResolver.INSTANCE)
                    .build();

            return template.apply(context);
        } catch (IOException e) {
            log.error("Error handling Handlebars templates", e);
            return null;
        }
    }

    /**
     * Overload the compile method to accept a JSON string with the values.
     */
    public String compile(String tmplName, String jsonStr) {
        try {
            return compile(tmplName, SpecObjectMapper.getInstance().readValue(jsonStr, JsonNode.class));
        } catch (IOException e) {
            log.error("Error parsing JSON template", e);
            return null;
        }
    }

    /**
     * Overload the compile method to accept a JSON file with the values.
     */
    public String compile(String tmplName, File jsonFile) {
        try {
            return compile(tmplName, Strings.fromFile(jsonFile));
        } catch (IOException e) {
            log.error("Error reading template file", e);
            return null;
        }
    }
}
