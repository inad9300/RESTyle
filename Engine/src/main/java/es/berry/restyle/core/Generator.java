package es.berry.restyle.core;

import es.berry.restyle.specification.Spec;

public abstract class Generator {

    protected Spec spec;
    protected TemplateGen tmpl = null;

    // TODO: either add "output directory" to the spec., or include it as an argument
    protected Generator(Spec spec) {
        this.spec = spec;
    }

    protected void setTmpl(TemplateGen tmpl) {
        this.tmpl = tmpl;
    }

    public abstract void generate();

}
