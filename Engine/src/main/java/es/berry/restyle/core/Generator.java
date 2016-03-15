package es.berry.restyle.core;

import es.berry.restyle.specification.Spec;

public abstract class Generator {

    protected Spec spec;

    // TODO: either add "output directory" to the spec., or include it as an argument
    protected Generator(Spec spec) {
        this.spec = spec;
    }

    public abstract void generate();

}
