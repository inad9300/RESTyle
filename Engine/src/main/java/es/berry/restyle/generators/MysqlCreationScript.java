package es.berry.restyle.generators;

// NOTE: should require the following command options: --output-file
// IDEA: templates vs. pure Java-based solution? A bit of both

import es.berry.restyle.core.Generator;
import es.berry.restyle.specification.Field;
import es.berry.restyle.specification.Resource;
import es.berry.restyle.specification.Spec;
import es.berry.restyle.utils.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MysqlCreationScript extends Generator {
    private static final String REVERSE_QUOTE = "`";

    // TODO: verify:
    // - Table names length
    // - "The maximum number of digits (M) for DECIMAL is 65. The maximum number of supported decimals (D) is 30.
    //    If D is omitted, the default is 0. If M is omitted, the default is 10."

    public MysqlCreationScript(Spec spec) {
        super(spec);
    }

    @Override
    public void generate() {
        String result = getInitialConfig();

        for (Resource r : spec.getResources())
            result += doResourcePart(r);

        try {
            Strings.toFile(result, "generate_database.sql");
        } catch (IOException e) {
            // FIXME: use logging system
            System.out.println("Error creating file in plugin " + this.getClass().getSimpleName()
                    + ": " + e.getMessage());
        }
    }

    private String createUsersAndGrantPrivileges() {
        // GRANT ALL PRIVILEGES ON `my_db`.* TO `username`@localhost IDENTIFIED BY 'password';
        return null;
    }

    private String getInitialConfig() { // TODO: include collation in spec
        final String charset = MysqlHelper.adaptStandardName(spec.getEncoding());
        String s = "SET NAMES " + charset + " COLLATE utf8_unicode_ci;\n\n"
                + "CREATE DATABASE IF NOT EXISTS `" + spec.getDatabase().getName() + "`\n"
                + "\tDEFAULT CHARACTER SET " + charset + "\n"
                + "\tDEFAULT COLLATE utf8_unicode_ci;\n\n"
                + "USE `" + spec.getDatabase().getName() + "`;";
        return s;
    }

    private String createTable(Resource r) {
        List<String> s = new ArrayList<String>();
        s.add("CREATE TABLE IF NOT EXISTS" + Strings.surround(r.getName(), REVERSE_QUOTE) + " (");

        if (r.getIdInjection())
            s.add("\t`id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY");

        Set<Field> fields = r.getFields();
        assert fields != null;

//        r.getAbstract();
//        r.getInheritanceStrategy();
//        r.getBase();
//        r.getAcceptExtraFields();
//        r.getAcl();
//        r.getCheck();
//        r.getIndex();
//        r.getPaginable();
//        r.getRelations();

//        for (Field field : fields) {
//            s += "\t" + "`" + field.getName() + "` "
//                    + MysqlHelper.getType(field) + " "
//                    + MysqlHelper.getTypeModifier(field) + " "
//                    + getMysqlForeignKey(field);
//        }
//
//        s += getMysqlChecks(fields);
//
//        s += ");";

        return Strings.join(s, "\n");
    }

    private String doResourcePart(Resource r) {
        String s = createTable(r);
        return s;
    }

    private String getMysqlForeignKey(Field field) {
        return null;
    }

    private List<String> getMysqlFieldChecks(Field field) {
        List<String> checks = new ArrayList<String>();

        // TODO: check validity for all data types (e.g. dates)

        if (field.getMax() != null)
            checks.add(field.getName() + " <= " + field.getMax());

        if (field.getMin() != null)
            checks.add(field.getName() + " >= " + field.getMin());

        return checks;
    }

    private String getMysqlChecks(List<Field> fields) {
        List<String> checks = new ArrayList<String>();

        for (Field field : fields)
            checks.addAll(getMysqlFieldChecks(field));

        // TODO: support more cases (e.g. start_date < end_date)

        return "CHECK (" + Strings.join(checks, " AND ") + ")";
    }

    /*private String createOneToOneRelation() { // FIXME: wrong name
        String stmt = "ALTER TABLE `" + subresource + "`"
                + "\nADD FOREIGN KEY (`id`) REFERENCES `" + resource + "`(id)";

        if (resource#1 subresource with "on_delete")
            stmt += "\nON DELETE " + on_delete;

        return stmt + ";";
    }*/

    /*private String createOneToManyRelation() {
        final String pkType = "INT UNSIGNED";
        final String pkColPrefix = "id_";
        final String newCol = pkColPrefix + resource;

        String stmt = "ALTER TABLE `" + resource + "`"
                + "\nADD COLUMN `" + newCol + "` " + pkType;

        if (required relationship)
            stmt += " NOT NULL";

        stmt += ";"
                + "\nALTER TABLE `" + resource + "`"
                + "\nADD FOREIGN KEY (`" + newCol + "`) REFERENCES `" + resource + "`(id)";

        if (resource#1 subresource with "on_delete")
            stmt += "\nON DELETE " + on_delete;

        return stmt + ";";
    }*/

    private String createManyToManyRelation() {
        String stmt;
        /*
        var subscript = [];

        var relation    = models[modelName].$_many[modelRefName];
        var relationRef = models[modelRefName].$_many[modelName];

        var newCol    = 'id_' + modelName;
        var newColRef = 'id_' + modelRefName;

        var subscript = [
            'CREATE TABLE IF NOT EXISTS `' + (modelName + '_' + modelRefName) + '` (',
            '\t`' + newCol    + '` INT UNSIGNED' + (!relation.required    ? '' : ' NOT NULL') + ',',
            '\t`' + newColRef + '` INT UNSIGNED' + (!relationRef.required ? '' : ' NOT NULL') + ','
        ];

        subscript = subscript.concat([
            '\tFOREIGN KEY (`' + newCol + '`)',
            '\t\tREFERENCES `' + modelName + '`(id)'
        ]);

        if (relation.on_delete) {
            subscript.push( '\t\tON DELETE ' + relation.on_delete.replace(/\s/g, '_').toUpperCase() );
        }
        subscript[subscript.length - 1] += ',';

        subscript = subscript.concat([
            '\tFOREIGN KEY (`' + newColRef + '`)',
            '\t\tREFERENCES `' + modelRefName + '`(id)'
        ]);

        if (relationRef.on_delete) {
            subscript.push( '\t\tON DELETE ' + relationRef.on_delete.replace(/\s/g, '_').toUpperCase() );
        }

        subscript.push(');');

        return subscript.join('\n');
        */
        return null;
    }
}

/*

function createManyRelation(modelName, modelRefName) {
    if (!models[modelRefName].$_many || !models[modelRefName].$_many[modelName]) {
        return Model.createOneToManyRelation(modelName, modelRefName);
    }
    else if (!models[modelRefName].$_many[modelName].$_done && // Avoid duplicating M:M tables
             !models[modelName].$_many[modelRefName].$_done) {
        models[modelName].$_many[modelRefName].$_done = true;
        models[modelRefName].$_many[modelName].$_done = true;

        return Model.createManyToManyRelation(modelName, modelRefName);
    }
    return '';
}

 */
