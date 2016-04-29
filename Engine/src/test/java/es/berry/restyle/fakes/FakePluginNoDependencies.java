package es.berry.restyle.fakes;

import es.berry.restyle.core.Generator;
import es.berry.restyle.specification.generated.Spec;

import java.io.File;

public class FakePluginNoDependencies extends Generator {
    public FakePluginNoDependencies(Spec s, File o) {
        super(s, o);
    }

    @Override
    public void generate() {
    }
}
