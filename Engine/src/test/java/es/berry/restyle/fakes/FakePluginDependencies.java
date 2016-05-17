package es.berry.restyle.fakes;

import com.fasterxml.jackson.databind.JsonNode;
import es.berry.restyle.core.Generator;
import es.berry.restyle.specification.generated.Spec;

import java.io.File;

public class FakePluginDependencies extends Generator {
    public FakePluginDependencies(Spec s, JsonNode j, File o) {
        super(s, j, o);
        this.prevGeneratorMustImplement(FakePluginI.class);
    }

    @Override
    public void generate() {
    }
}
