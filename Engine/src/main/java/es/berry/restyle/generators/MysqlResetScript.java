package es.berry.restyle.generators;

public class MysqlResetScript {

    private String dbName;

    public MysqlResetScript(String dbName) {
        this.dbName = dbName;
    }

    public String deleteDatabase() {
        return "DROP DATABASE IF EXISTS `" + this.dbName + "`;\n";
    }

}
