package es.berry.restyle.core;

// Helper class for Handlebars functions

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import es.berry.restyle.logging.Log;
import es.berry.restyle.logging.Logger;
import es.berry.restyle.utils.Strings;

import java.io.File;
import java.io.IOException;

public class TemplateGen {

    public static String TMPL_DIR = "./src/main/resources/templates/";

    private Class associatedClass = null;

    private Handlebars handlebars;

    private static final Logger log = Log.getChain();

    private void commonConstruct(String dir, String ext) {
        final TemplateLoader loader = new FileTemplateLoader(dir, tuneExtension(ext));
        this.handlebars = new Handlebars(loader);
        handlebars.registerHelper("json", Jackson2Helper.INSTANCE);
    }

    private String tuneExtension(String ext) {
        if (Strings.isEmpty(ext))
            return ".hbs"; // Default value for handlebars

        return ext.startsWith(".") ? ext : "." + ext;
    }

    public String getDefDir() {
        if (this.associatedClass == null)
            return TMPL_DIR;

        return TMPL_DIR + this.associatedClass.getSimpleName() + File.separator;
    }

    public TemplateGen(Class c) {
        this.associatedClass = c;
        commonConstruct(getDefDir(), null);
    }

    public TemplateGen(Class c, String ext) {
        this.associatedClass = c;
        commonConstruct(getDefDir(), ext);
    }

    public TemplateGen(String dir) {
        commonConstruct(dir, null);
    }

    public TemplateGen(String dir, String ext) {
        commonConstruct(dir, ext);
    }

    /* Example of ObjectNode (extends JsonNode) creation:
        ObjectNode root = new ObjectMapper().createObjectNode();
        root.put("tableName", "test_table_name");
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

    public String compile(String tmplName, String jsonStr) {
        try {
            return compile(tmplName, new ObjectMapper().readValue(jsonStr, JsonNode.class));
        } catch (IOException e) {
            log.error("Error parsing JSON template", e);
            return null;
        }
    }

    public String compile(String tmplName, File jsonFile) {
        try {
            return compile(tmplName, Strings.fromFile(jsonFile));
        } catch (IOException e) {
            log.error("Error reading template file", e);
            return null;
        }
    }
}
