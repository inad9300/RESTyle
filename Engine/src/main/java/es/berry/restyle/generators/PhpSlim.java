package es.berry.restyle.generators;

import es.berry.restyle.core.Generator;
import es.berry.restyle.core.TemplateGen;
import es.berry.restyle.generators.interfaces.SqlCarrier;
import es.berry.restyle.specification.generated.Spec;

import java.io.File;

public class PhpSlim extends Generator {

    protected PhpSlim(Spec spec, File outputDir) {
        super(spec, outputDir);
        this.setTemplateGen( new TemplateGen(PhpSlim.class, "php") );
        // this.setPrevGenerator(SqlCarrier.class);
    }

    @Override
    public void generate() {

    }
}
