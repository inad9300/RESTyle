package es.berry.restyle.generators;

import es.berry.restyle.core.Generator;
import es.berry.restyle.specification.generated.Spec;

public class MysqlResetScript extends Generator {

    @Override
    public void generate() {}

    private String deleteDatabase() {
        return "DROP DATABASE IF EXISTS `" + "dbName" + "`;\n";
    }
}
