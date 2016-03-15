import es.berry.restyle.generators.Generation;
import es.berry.restyle.specification.Spec;
import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

public class MysqlDdlGen extends Plugin {

    public MysqlDdlGen(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public static class DdlGeneration implements Generation {
        public String generate(Spec spec) {
            return "Testing DDL generation";
        }
    }
}
