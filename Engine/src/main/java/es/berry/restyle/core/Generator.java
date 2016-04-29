package es.berry.restyle.core;

import es.berry.restyle.logging.Log;
import es.berry.restyle.logging.Logger;
import es.berry.restyle.specification.generated.Spec;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Class to inherit from when implementing a plugin. Provides direct access to the specification, the output directory,
 * an instance of the TemplateGen class, and some logic to handle the dependency with another plugin.
 */
public abstract class Generator {

    final private Spec spec;
    final private File out;
    private TemplateGen templateGen = null;
    private Class prevGeneratorInterface = null;
    private Generator prevGenerator = null;

    private static Logger log = Log.getChain();

    /**
     * Interface method to be implemented by any class claiming to be a Generator.
     */
    public abstract void generate();

    protected Generator(Spec spec, File outputDir) {
        this.spec = spec;
        this.out = outputDir;
    }

    public Spec getSpec() {
        return spec;
    }

    public File getOut() {
        return out;
    }

    public TemplateGen getTemplateGen() {
        return templateGen;
    }

    public void setTemplateGen(TemplateGen template) {
        this.templateGen = template;
    }

    protected void prevGeneratorMustImplement(Class c) {
        this.prevGeneratorInterface = c;
    }

    // Method synonym, for consistency
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

    // Since the dependencies are set dynamically via Reflections, we need a way to execute methods dynamically too
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

    // To invoke methods with no parameters
    protected Object invokePrevMethod(String methodName) {
        return invokePrevMethod(methodName, null);
    }

    public static void runAll(List<Class<? extends Generator>> concreteGenerators, Spec spec, File outputDir) {
        Generator prevGen = null;

        for (Class<? extends Generator> genClass : concreteGenerators)
            try {
                log.info("Executing plugin " + genClass.getSimpleName() + "...");
                Generator gen = genClass.getConstructor(Spec.class, File.class).newInstance(spec, outputDir);

                if (prevGen == null && gen.getPrevGeneratorInterface() != null)
                    throw new RuntimeException("The plugin " + genClass.getSimpleName()
                            + " depends on a previous plugin, but none was provided");

//                System.out.println("CURR. CLASS: " + genClass.getSimpleName());
//                System.out.println("PREV. CLASS: " + gen.getPrevGeneratorInterface());

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
}
