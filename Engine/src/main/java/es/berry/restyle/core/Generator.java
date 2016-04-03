package es.berry.restyle.core;

import es.berry.restyle.specification.generated.Spec;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public abstract class Generator {

    final private Spec spec;
    final private File out;
    private TemplateGen templateGen;
    private Generator prevGenerator;
    // private List<Class<Generator>> prevGeneratorInterfaces;

    protected Generator(Spec spec, File outputDir) {
        this.spec = spec;
        this.out = outputDir;
        this.templateGen = null;
        this.prevGenerator = null;
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

//    protected void doSetPrevGenerator(Generator gen) {
//        this.prevGenerator = gen;
//    }

    // Way for the plugins to specify the interface their previous generator must implement
//    protected void setPrevGenerator(Class<? extends Generator> gen) {
//        // this.prevGeneratorInterfaces = Arrays.asList(gen);
//    }


    /* package-private */ Generator doGenerate() {
        generate();
        return this;
    }

    /**
     * Interface method to be implemented by any class claiming to be a Generator.
     */
    public abstract void generate();
}
