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

import java.io.File;
import java.io.IOException;

/**
 * Wrapper class for the Handlebars.java library.
 * <p>
 * Documentation at https://jknack.github.io/handlebars.java and https://github.com/jknack/handlebars.java
 */
public class TemplateGen {

    public static String TMPL_DIR = Config.getResourcePath("templates/");

    private Class associatedClass = null;

    private Handlebars handlebars;

    private static final Logger log = Log.getChain();

    /**
     * Common part of the construction phase, just to avoid some code duplication in the constructor.
     */
    private void commonConstruct(String dir, String ext) {
        final File d = new File(dir);
        if (!d.mkdirs() && !d.exists())
            log.warn("Could not to create templates' directory " + d.getAbsolutePath());

        final TemplateLoader loader = new FileTemplateLoader(dir, tuneExtension(ext));
        this.handlebars = new Handlebars(loader);
        handlebars.registerHelper("json", Jackson2Helper.INSTANCE);
    }

    /**
     * Ensure that an extension is valid.
     */
    private String tuneExtension(String ext) {
        if (Strings.isEmpty(ext))
            return ".hbs"; // Default value for handlebars

        return ext.startsWith(".") ? ext : "." + ext;
    }

    /**
     * Return the directory where the templates are supposed to be by default.
     */
    public String getDefaultDir() {
        if (this.associatedClass == null)
            return TMPL_DIR;

        return TMPL_DIR + this.associatedClass.getSimpleName() + File.separator;
    }

    public TemplateGen(Class c) {
        this.associatedClass = c;
        commonConstruct(getDefaultDir(), null);
    }

    public TemplateGen(Class c, String ext) {
        this.associatedClass = c;
        commonConstruct(getDefaultDir(), ext);
    }

    public TemplateGen(String dir) {
        commonConstruct(dir, null);
    }

    public TemplateGen(String dir, String ext) {
        commonConstruct(dir, ext);
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
            Template template = handlebars.compile(tmplName);

            Context context = Context.newBuilder(jsonNode).resolver(
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
