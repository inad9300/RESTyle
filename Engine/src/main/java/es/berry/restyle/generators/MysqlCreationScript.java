package es.berry.restyle.generators;

// NOTE: should require the following command options: --output-file
// IDEA: templates vs. pure Java-based solution?

import es.berry.restyle.configuration.Field;
import es.berry.restyle.utils.Strings;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MysqlCreationScript {

    // Integers. Reference: http://dev.mysql.com/doc/refman/5.7/en/integer-types.html
    // Signed
    private static final int S_TINY_MIN = -128;
    private static final int S_TINY_MAX = 127;
    private static final int S_SMALL_MIN = -32768;
    private static final int S_SMALL_MAX = 32767;
    private static final int S_MEDIUM_MIN = -8388608;
    private static final int S_MEDIUM_MAX = 8388607;
    private static final int S_INT_MIN = -2147483648;
    private static final int S_INT_MAX = 2147483647;
    private static final long S_BIG_MIN = -9223372036854775808L;
    private static final long S_BIG_MAX = 9223372036854775807L;
    // Unsigned
    private static final int U_TINY_MAX = 255;
    private static final int U_SMALL_MAX = 65535;
    private static final int U_MEDIUM_MAX = 16777215;
    private static final long U_INT_MAX = 4294967295L;
    private static final BigInteger U_BIG_MAX = new BigInteger("18446744073709551615");

    // Blobs and texts. Reference: http://dev.mysql.com/doc/refman/5.7/en/storage-requirements.html
    private static final int TINY_MAX = 255;
    private static final int TEXT_MAX = 65535;
    private static final int MEDIUM_MAX = 16777215;
    private static final long LONG_MAX = 4294967295L;

    private static final int MAX_BYTES_UTF8 = 3;
    private static final int MAX_BYTES_UTF8MB4 = 4;

    private static final int CHAR_MAX_CHARS = 255;
    private static final int VARCHAR_MAX_BYTES = 65535;

    public MysqlCreationScript() {
        // TODO: verify:
        // - Table names length
        // - "The maximum number of digits (M) for DECIMAL is 65. The maximum number of supported decimals (D) is 30. If D is omitted, the default is 0. If M is omitted, the default is 10."
        // - etc.
    }

    private static String getMysqlType(Field field) {
        String ownType = field.getType();

        if (ownType.equals("int")) {
            if (field.getMin() == null && field.getMax() == null)
                return "INT";

            // TODO: what if one or the other is null?

            if (field.getMin() < 0) { // Signed
                if (field.getMin() >= S_TINY_MIN && field.getMax() <= S_TINY_MAX)
                    return "TINYINT";
                else if (field.getMin() >= S_SMALL_MIN && field.getMax() <= S_SMALL_MAX)
                    return "SMALLINT";
                else if (field.getMin() >= S_MEDIUM_MIN && field.getMax() <= S_MEDIUM_MAX)
                    return "MEDIUMINT";
                else if (field.getMin() >= S_INT_MIN && field.getMax() <= S_INT_MAX)
                    return "INT";
                else if (field.getMin() >= S_BIG_MIN && field.getMax() <= S_BIG_MAX)
                    return "BIGINT";
                else
                    throw new IllegalArgumentException(
                            "The int values must be between " + S_BIG_MIN + " and " + S_BIG_MAX + ". Given minimum and maximum: "
                                    + field.getMin() + " and " + field.getMax() + ".");
            } else { // Unsigned
                String mysqlType;

                if (field.getMax() == null)
                    mysqlType = "INT";
                else if (field.getMax() <= U_TINY_MAX)
                    mysqlType = "TINYINT";
                else if (field.getMax() <= U_SMALL_MAX)
                    mysqlType = "SMALLINT";
                else if (field.getMax() <= U_MEDIUM_MAX)
                    mysqlType = "MEDIUMINT";
                else if (field.getMax() <= U_INT_MAX)
                    mysqlType = "INT";
                else if (U_BIG_MAX.compareTo(new BigInteger(field.getMax().toString())) <= 0)
                    mysqlType = "BIGINT";
                else
                    throw new IllegalArgumentException(
                            "The maximum value of an unsigned int cannot be greater than " + U_BIG_MAX + ". Given: "
                                    + field.getMax() + ".");

                return mysqlType + " UNSIGNED";
            }
        } else if (ownType.equals("float")) {
            String mysqlType = "FLOAT";

            // MySQL automatically uses FLOAT for precisions between 0 and 23, and DOUBLE if it is between 24 and 53
            if (field.getPrecision() != null) {
                if (field.getPrecision().length == 1) // IDEA: allow simple integers (not in an array)
                    mysqlType += " (" + field.getPrecision()[0] + ")";
                else if (field.getPrecision().length == 2)
                    mysqlType += " (" + field.getPrecision()[0] + ", " + field.getPrecision()[1] + ")";
                else
                    throw new IllegalArgumentException("Wrong format when defining the precision.");
            }

            if (field.getMin() < 0)
                mysqlType += " UNSIGNED";

            return mysqlType;
        } else if (ownType.equals("decimal")) {
            String mysqlType = "DECIMAL";

            if (field.getPrecision() != null)
                mysqlType += " (" + field.getPrecision()[0] + ", " + field.getPrecision()[1] + ")";

            if (field.getMin() < 0)
                mysqlType += " UNSIGNED";

            return mysqlType;
        } else if (ownType.equals("string")) {
            if (field.getIn() != null) {
                String mysqlType = "ENUM(";

                List<String> inValues = new ArrayList<String>();
                for (Object val : field.getIn())
                    inValues.add(val.toString());

                final String separator = ", ";

                // From http://dev.mysql.com/doc/refman/5.7/en/enum.html: "To prevent unexpected results when using the
                // ORDER BY clause on an ENUM column, (...) Specify the ENUM list in alphabetic order."
                Collections.sort(inValues);

                for (String val : inValues)
                    mysqlType += val + ", ";

                mysqlType.substring(0, mysqlType.length() - separator.length()); // Remove last separator

                mysqlType += ")";
                return mysqlType;
            }

            if (field.getMax() == field.getMin() && field.getMin() <= CHAR_MAX_CHARS)
                return "CHAR (" + field.getMin() + ")";

            // NOTE: "BLOB and TEXT columns cannot have DEFAULT values."

            int minLen = field.getMin() * MAX_BYTES_UTF8MB4;
            int maxLen = field.getMax() * MAX_BYTES_UTF8MB4;

            // NOTE: by now, TEXT_MAX = VARCHAR_MAX_BYTES = 65535 bytes, so TINYTEXT and TEXT will not be chosen
            if (maxLen <= VARCHAR_MAX_BYTES)
                return "VARCHAR (" + minLen + ")";
            else if (maxLen < TINY_MAX)
                return "TINYTEXT";
            else if (maxLen < TEXT_MAX)
                return "TEXT";
            else if (maxLen < MEDIUM_MAX)
                return "MEDIUMTEXT";
            else if (maxLen < LONG_MAX)
                return "LONGTEXT";
            else
                throw new RuntimeException("...");
        } else if (ownType.equals("bool")) {
            return "BIT(1)";
        } else if (ownType.equals("date")) {
            // Reference: http://dev.mysql.com/doc/refman/5.7/en/datetime.html
            return "DATETIME";
        } else if (ownType.equals("blob")) {
            // TODO: normalize spec. to include always bytes (accept different units)
            if (field.getMax() < TINY_MAX)
                return "TINYBLOB";
            else if (field.getMax() < TEXT_MAX)
                return "BLOB";
            else if (field.getMax() < MEDIUM_MAX)
                return "MEDIUMBLOB";
            else if (field.getMax() < LONG_MAX)
                return "LONGBLOB";
            else
                throw new RuntimeException("...");
        } else {
            throw new RuntimeException("The type provided for the field " + field.getName() + " is not valid. Given: " +
                    ownType + ". Please, consider reworking the validation algorithm for the data specification.");
        }
    }

    private static String getMysqlTypeModifier(Field field) {
        List<String> modifiers = new ArrayList<String>();

        if (field.isRequired())
            modifiers.add("NOT NULL");

        if (field.getDefaultExpression() != null)
            modifiers.add("DEFAULT " + field.getDefaultExpression());

        if (field.isUnique())
            modifiers.add("UNIQUE");

        return Strings.join(modifiers, " ");
    }

    private static String getMysqlForeignKey(Field field) {
        return null;
    }

    private static List<String> getMysqlFieldChecks(Field field) {
        List<String> checks = new ArrayList<String>();

        // TODO: check validity for all data types (e.g. dates)

        if (field.getMax() != null)
            checks.add(field.getName() + " <= " + field.getMax());

        if (field.getMin() != null)
            checks.add(field.getName() + " >= " + field.getMin());

        return checks;
    }

    private static String getMysqlChecks(List<Field> fields) {
        List<String> checks = new ArrayList<String>();

        for (Field field : fields)
            checks.addAll(getMysqlFieldChecks(field));

        // TODO: support more cases (e.g. start_date < end_date)

        return "CHECK (" + Strings.join(checks, " AND ") + ")";
    }

    public String initialConfig() {
        String s = "SET NAMES utf8 COLLATE utf8_unicode_ci;\n\n"
                + "CREATE DATABASE IF NOT EXISTS `" + "config.database.name" + "`\n"
                + "\tDEFAULT CHARACTER SET utf8\n"
                + "\tDEFAULT COLLATE utf8_unicode_ci;\n\n"

                + "USE `" + "config.database.name" + "`;";
        // GRANT ALL PRIVILEGES ON `my_db`.* TO `username`@localhost IDENTIFIED BY 'password';
        return s;
    }

    public String createTable(String name) {
        String s = "CREATE TABLE IF NOT EXISTS `" + name + "` (\n"
                + "\t`id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,\n"; // Ending with comma since we assume more values

        List<Field> fields = null;
        // IDEA (?): assert fields != null;
        for (Field field : fields) {
            s += "\t" + "`" + field.getName() + "` "
                    + getMysqlType(field) + " "
                    + getMysqlTypeModifier(field) + " "
                    + getMysqlForeignKey(field);
        }

        s += getMysqlChecks(fields);

        s += ");";

        return s;
    }

    public String createOneToOneRelation() { // FIXME: wrong name
        String stmt = "ALTER TABLE `" + subresource + "`"
                + "\nADD FOREIGN KEY (`id`) REFERENCES `" + resource + "`(id)";

        if (/* resource#1 subresource with "on_delete" */ true)
            stmt += "\nON DELETE " + on_delete;

        return stmt + ";";
    }

    public String createOneToManyRelation() {
        final String pkType = "INT UNSIGNED";
        final String pkColPrefix = "id_";
        final String newCol = pkColPrefix + resource;

        String stmt = "ALTER TABLE `" + resource + "`"
                + "\nADD COLUMN `" + newCol + "` " + pkType;

        if (/* required relationship */ true)
            stmt += " NOT NULL";

        stmt += ";"
                + "\nALTER TABLE `" + resource + "`"
                + "\nADD FOREIGN KEY (`" + newCol + "`) REFERENCES `" + resource + "`(id)";

        if (/* resource#1 subresource with "on_delete" */true)
            stmt += "\nON DELETE " + on_delete;

        return stmt + ";";
    }

    public String createManyToManyRelation() {
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
