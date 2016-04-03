package es.berry.restyle.generators;

import es.berry.restyle.core.Generator;
import es.berry.restyle.specification.generated.Spec;

import java.io.File;

public class MysqlResetScript extends Generator {

    protected MysqlResetScript(Spec spec, File outputDir) {
        super(spec, outputDir);
    }

    @Override
    public void generate() {}

    private String deleteDatabase() {
        return "DROP DATABASE IF EXISTS `" + "dbName" + "`;\n";
    }
}
