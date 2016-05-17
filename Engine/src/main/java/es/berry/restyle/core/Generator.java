package es.berry.restyle.core;

import com.fasterxml.jackson.databind.JsonNode;
import es.berry.restyle.logging.Log;
import es.berry.restyle.logging.Logger;
import es.berry.restyle.specification.SpecObjectMapper;
import es.berry.restyle.specification.generated.Spec;
import es.berry.restyle.utils.Json;
import es.berry.restyle.utils.Strings;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Class to inherit from when implementing a plugin. Provides direct access to the specification, the output directory,
 * an instance of the TemplateGen class, and some logic to handle the dependency with another plugin.
 */
public abstract class Generator {

    final private Spec spec;
    final private JsonNode specNode;
    final private File out;
    private TemplateGen templateGen = null;
    private Class prevGeneratorInterface = null;
    private Generator prevGenerator = null;

    private static Logger log = Log.getChain();

    private static final String PLUGIN_SCHEMAS_PATH = "./src/main/resources/schemas/";

    /**
     * Interface method to be implemented by any class claiming to be a Generator, where the magic (namely, the
     * generation of artifacts useful for the final user) is supposed to happen.
     */
    public abstract void generate();

    protected Generator(Spec spec, JsonNode specNode, File outputDir) {
        this.spec = spec;
        this.specNode = specNode;
        this.out = outputDir;
    }

    public Spec getSpec() {
        return this.spec;
    }

    /**
     * Return the original specification, allowing access to the raw data, and (particularly) to plugin-defined, custom
     * properties. Notice that this representation lacks some information added afterwards to the Spec class.
     */
    public JsonNode getSpecNode() {
        return this.specNode;
    }

    public File getOut() {
        return this.out;
    }

    public TemplateGen getTemplateGen() {
        return templateGen;
    }

    public void setTemplateGen(TemplateGen template) {
        this.templateGen = template;
    }

    /**
     * Define a dependency with a plugin. This way, a plugin can force the previous plugin to implement a certain
     * Interface that it expects, and otherwise abnegate to execute itself.
     */
    protected void prevGeneratorMustImplement(Class c) {
        this.prevGeneratorInterface = c;
    }

    /**
     * Synonym of prevGeneratorMustImplement, for the shake of consistency.
     */
    protected void setPrevGeneratorInterface(Class c) {
        this.prevGeneratorMustImplement(c);
    }

    protected Class getPrevGeneratorInterface() {
        return this.prevGeneratorInterface;
    }

    protected Generator getPrevGenerator() {
        return this.prevGenerator;
    }

    protected void setPrevGenerator(Generator gen) {
        this.prevGenerator = gen;
    }

    /**
     * Attempt to simplify the way to execute methods on the previous plugin. This is needed since the dependencies are
     * set dynamically via Reflections, so we don't have information at compile time about the methods the previous
     * generator has.
     */
    protected Object invokePrevMethod(String methodName, Class[] paramTypes, Object... paramValues) {
        if (this.getPrevGenerator() == null)
            throw new RuntimeException("Trying to invoke a method on the previous generator, but there is no previous generator");

        try {
            final Class c = this.getPrevGenerator().getClass();
            if (paramTypes == null)
                return c.getMethod(methodName).invoke(this.prevGenerator);
            else
                return c.getMethod(methodName, paramTypes).invoke(this.prevGenerator, paramValues);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Not possible to access method \"" + methodName + "\" on the previous generator");
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Not possible to invoke method \"" + methodName + "\" on the previous generator");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Method \"" + methodName + "\" does not exist on the previous generator");
        }
    }

    /**
     * Overload invokePrevMethod to allow invoking methods with no parameters.
     */
    protected Object invokePrevMethod(String methodName) {
        return invokePrevMethod(methodName, null);
    }

    /**
     * Hold the logic to run all the plugins in a series, checking that the existing dependencies are met, and building
     * the chain of generators by passing the previous one to the next. Plus, it establish a "JSON Schema validation"
     * phase, where plugin-specific restrictions are validated, if any.
     */
    public static void runAll(List<Class<? extends Generator>> concreteGenerators, Spec spec, JsonNode specNode, File outputDir) {
        Generator prevGen = null;

        for (Class<? extends Generator> genClass : concreteGenerators)
            try {
                log.info("Executing plugin " + genClass.getSimpleName() + "...");
                Generator gen = genClass.getConstructor(Spec.class, JsonNode.class, File.class).newInstance(spec, specNode, outputDir);

                if (prevGen == null && gen.getPrevGeneratorInterface() != null)
                    throw new RuntimeException("The plugin " + genClass.getSimpleName()
                            + " depends on a previous plugin, but none was provided");

                validateJsonSchema(gen);

                // Checking if the interface is being actually implemented by the predecessor
                if (prevGen != null && gen.getPrevGeneratorInterface() != null &&
                        !gen.getPrevGeneratorInterface().isAssignableFrom(prevGen.getClass()))
                    throw new RuntimeException(
                            "The plugin " + genClass.getSimpleName() + ", which depends on the plugin "
                                    + prevGen.getClass().getSimpleName() + ", needs it to implement the interface "
                                    + gen.getPrevGeneratorInterface().getSimpleName() + ", which it's not happening");

                gen.setPrevGenerator(prevGen);
                gen.generate();
                prevGen = gen;
            } catch (InstantiationException e) {
                log.broke("Impossible to instantiate plugin class " + genClass.getSimpleName(), e);
            } catch (IllegalAccessException e) {
                log.broke("Impossible to access method", e);
            } catch (InvocationTargetException e) {
                log.broke("Impossible to invoke method", e);
            } catch (NoSuchMethodException e) {
                log.broke("Impossible to find method", e);
            }
    }

    /**
     * In this phase, which all the plugins pass, the specification is validated against a plugin-specific JSON Schema,
     * if it is present under "./src/main/resources/schemas/" as "{{pluginClassBasicName}}.json". There, they can define
     * either more restrictions (e.g. for attribute values) or new, custom attributes.
     */
    private static void validateJsonSchema(Generator gen) {
        final File jsonSchemaFile = new File(PLUGIN_SCHEMAS_PATH + gen.getClass().getSimpleName() + ".json");

        if (jsonSchemaFile.exists() && jsonSchemaFile.isFile()) {
            log.info("· Validating against JSON schema...");

            final String report = Json.validateAgainstSchema(
                    SpecObjectMapper.getInstance().valueToTree(gen.getSpec()),
                    jsonSchemaFile.getAbsolutePath()
            );

            if (!Strings.isEmpty(report)) {
                log.error(report);
                System.exit(1);
            }
        }
    }
}
