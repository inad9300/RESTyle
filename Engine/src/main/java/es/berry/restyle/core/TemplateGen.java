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
import es.berry.restyle.utils.Strings;

import java.io.File;
import java.io.IOException;

public class TemplateGen {

    public static String TMPL_DIR = "./src/main/resources/templates/";

    private Handlebars handlebars;

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

    public TemplateGen(Class c) {
        commonConstruct(TMPL_DIR + c.getSimpleName(), null);
    }

    public TemplateGen(Class c, String ext) {
        commonConstruct(TMPL_DIR + c.getSimpleName(), ext);
    }

    public TemplateGen(String dir) {
        commonConstruct(dir, null);
    }

    public TemplateGen(String dir, String ext) {
        commonConstruct(dir, ext);
    }

    /* Example of JsonNode (ObjectNode) creation:
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
            e.printStackTrace();
            throw new RuntimeException("Error handling Handlebars templates: " + e.getMessage()); // FIXME: use logging system
        }
    }

    public String compile(String tmplName, String jsonStr) {
        try {
            return compile(tmplName, new ObjectMapper().readValue(jsonStr, JsonNode.class));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error parsing JSON template: " + e.getMessage()); // FIXME
        }
    }

    public String compile(String tmplName, File jsonFile) {
        try {
            return compile(tmplName, Strings.fromFile(jsonFile));
        } catch (IOException e) {
            throw new RuntimeException("Error reading template file: " + e.getMessage()); // FIXME
        }
    }
}
