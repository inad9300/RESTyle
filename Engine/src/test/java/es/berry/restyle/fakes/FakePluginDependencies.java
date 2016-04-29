package es.berry.restyle.fakes;

import es.berry.restyle.core.Generator;
import es.berry.restyle.specification.generated.Spec;

import java.io.File;

public class FakePluginDependencies extends Generator {
    public FakePluginDependencies(Spec s, File o) {
        super(s, o);
        this.prevGeneratorMustImplement(FakePluginI.class);
    }

    @Override
    public void generate() {
    }
}
