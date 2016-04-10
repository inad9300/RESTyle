package es.berry.restyle.core;

import es.berry.restyle.specification.generated.Spec;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Generator {

    final private Spec spec;
    final private File out;
    private TemplateGen templateGen = null;
    private Class prevGeneratorInterface = null;
    private Generator prevGenerator = null;

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

    // Since the dependencies are set dynamically, we need a way to execute methods dynamically too
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

    /**
     * Interface method to be implemented by any class claiming to be a Generator.
     */
    public abstract void generate();
}
