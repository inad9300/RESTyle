package es.berry.restyle.core;

import es.berry.restyle.specification.generated.Spec;

import java.io.File;

public abstract class Generator {

    final protected Spec spec;
    final protected File out;
    protected TemplateGen tmpl;

    protected Generator() {
        throw new RuntimeException("The Generator default constructor is not expected to be called.");
    }

    protected Generator(Spec spec, File outputDir) {
        this.spec = spec;
        this.out = outputDir;
        this.tmpl = null;
    }

    public abstract void generate();

}
