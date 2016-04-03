package es.berry.restyle.core;

import es.berry.restyle.specification.generated.Spec;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public abstract class Generator {

    final private Spec spec;
    final private File out;
    private TemplateGen templateGen = null;
    private Class prevGeneratorInterface = null;

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

    /**
     * Interface method to be implemented by any class claiming to be a Generator.
     */
    public abstract void generate();
}
