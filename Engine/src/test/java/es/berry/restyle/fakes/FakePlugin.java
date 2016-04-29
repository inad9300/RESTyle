package es.berry.restyle.fakes;

import es.berry.restyle.core.Generator;
import es.berry.restyle.specification.generated.Spec;

import java.io.File;

public class FakePlugin extends Generator implements FakePluginI {
    public FakePlugin(Spec s, File o) {
        super(s, o);
    }

    @Override
    public void generate() {
    }

    @Override
    public void doSomething() {
    }
}
