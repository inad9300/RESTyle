package es.berry.restyle.generators;

import es.berry.restyle.specification.Types;
import es.berry.restyle.specification.generated.Field;
import es.berry.restyle.utils.Strings;

import java.math.BigInteger;
import java.util.*;

public class MysqlHelper {

    public final static class InheritanceStrategies {
        public final static String SINGLE_TABLE = "singleTable";
        public final static String TABLE_PER_CONCRETE = "tablePerConcreteClass";
        public final static String TABLE_PER_CLASS = "tablePerClass";
    }

    // NOTE: to list the available character sets and their default collations with the SHOW CHARACTER SET statement.
    // NOTE: UTF-8 likely collations: utf8mb4_general_ci, utf8mb4_unicode_ci
    public static String adaptStandardName(String name) {
        final String UTF_8 = "utf-8";
        final Set<String> validCharsets = new HashSet<String>(Arrays.asList(UTF_8));

        name = name.toLowerCase();

        if (name.equals(UTF_8))
            return "utf8mb4";

        throw new IllegalArgumentException("The specified character set is not supported by this plugin. Choose one of "
                + Strings.join(validCharsets, ", ").toUpperCase());
    }

    public static String getTypeModifier(Field field) {
        List<String> modifiers = new ArrayList<String>();

        if (field.getRequired())
            modifiers.add("NOT NULL");
        else
            modifiers.add("NULL");

        final String SINGLE_QUOTE = "'";

        if (field.getDefault() != null) {
            final String defStr = field.getDefault().toString();
            final boolean needsQuotes = field.getType().equals(Types.STRING)
                    && !defStr.startsWith(SINGLE_QUOTE)
                    && !defStr.endsWith(SINGLE_QUOTE);
            modifiers.add("DEFAULT " + (needsQuotes ? Strings.surround(defStr, SINGLE_QUOTE) : field.getDefault()));
        }

        if (field.getAutoIncrement())
            modifiers.add("AUTO_INCREMENT");

        if (field.getUnique())
            modifiers.add("UNIQUE");

        if (Strings.isEmpty(field.getDescription()))
            modifiers.add("COMMENT " + Strings.surround(field.getDescription(), SINGLE_QUOTE));

        return Strings.join(modifiers, " ");
    }

    public static String getType(Field field) {
        // Integers. Reference: http://dev.mysql.com/doc/refman/5.7/en/integer-types.html
        // Signed
        final int S_TINY_MIN = -128;
        final int S_TINY_MAX = 127;
        final int S_SMALL_MIN = -32768;
        final int S_SMALL_MAX = 32767;
        final int S_MEDIUM_MIN = -8388608;
        final int S_MEDIUM_MAX = 8388607;
        final int S_INT_MIN = -2147483648;
        final int S_INT_MAX = 2147483647;
        final long S_BIG_MIN = -9223372036854775808L;
        final long S_BIG_MAX = 9223372036854775807L;
        // Unsigned
        final int U_TINY_MAX = 255;
        final int U_SMALL_MAX = 65535;
        final int U_MEDIUM_MAX = 16777215;
        final long U_INT_MAX = 4294967295L;
        final BigInteger U_BIG_MAX = new BigInteger("18446744073709551615");

        // Blobs and texts. Reference: http://dev.mysql.com/doc/refman/5.7/en/storage-requirements.html
        final int TINY_MAX = 255;
        final int TEXT_MAX = 65535;
        final int MEDIUM_MAX = 16777215;
        final long LONG_MAX = 4294967295L;

        final int MAX_BYTES_UTF8 = 3;
        final int MAX_BYTES_UTF8MB4 = 4;

        final int CHAR_MAX_CHARS = 255;
        final int VARCHAR_MAX_BYTES = 65535;

        String fieldType = field.getType();

        if (fieldType.equals(Types.INT)) {
            if (field.getMin() == null && field.getMax() == null)
                return "INT";

            if (field.getMin() == null)
                field.setMin((long) S_INT_MIN);

            if (field.getMax() == null)
                field.setMax(U_INT_MAX);

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
                            "The int values must be between " + S_BIG_MIN + " and " + S_BIG_MAX
                                    + ". Given minimum and maximum: " + field.getMin() + " and " + field.getMax() + ".");
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
                    throw new IllegalArgumentException("The maximum value for unsigned integers cannot be greater than "
                            + U_BIG_MAX + ". Given: " + field.getMax() + ".");

                return mysqlType + " UNSIGNED";
            }
        } else if (fieldType.equals(Types.FLOAT)
                || fieldType.equals(Types.DECIMAL)) {
            String mysqlType = fieldType.equals(Types.FLOAT) ? "FLOAT" : "DECIMAL";

            // IDEA: use min and max values to determine the precision! (either that or check the consistency of both)

            // NOTE: MySQL automatically uses FLOAT for precisions between 0 and 23, and DOUBLE if it is between 24 and 53
            if (field.getPrecision() != null) {
                assert field.getPrecision().size() == 2;

                if (fieldType.equals(Types.DECIMAL)) {
                    final int MAX_DECIMAL_DIGITS = 65;
                    final int MAX_DECIMAL_DECIMALS = 30;

                    if (field.getPrecision().get(0) > MAX_DECIMAL_DIGITS)
                        throw new IllegalArgumentException("MySQL does not allow decimal precisions bigger than "
                                + MAX_DECIMAL_DIGITS);

                    if (field.getPrecision().get(1) > MAX_DECIMAL_DECIMALS)
                        throw new IllegalArgumentException("MySQL does not allow decimals to have more than "
                                + MAX_DECIMAL_DECIMALS + " digits to the right of the decimal point.");

                    if (field.getPrecision().get(1) > field.getPrecision().get(0))
                        throw new IllegalArgumentException("The number of decimal digits cannot be bigger than the total "
                                + "number of them.");
                }

                mysqlType += " (" + field.getPrecision().get(0) + ", " + field.getPrecision().get(1) + ")";
            }



            if (field.getMin() >= 0)
                mysqlType += " UNSIGNED";

            return mysqlType;
        } else if (fieldType.equals(Types.STRING)) {
            if (field.getEnum() != null) {
                String mysqlType = "ENUM(";

                List<String> enumValues = new ArrayList<String>();
                for (Object val : field.getEnum())
                    if (val instanceof String)
                        enumValues.add((String) val);

                // NOTE: from http://dev.mysql.com/doc/refman/5.7/en/enum.html: "To prevent unexpected results when
                // using the ORDER BY clause on an ENUM column, (...) Specify the ENUM list in alphabetic order"
                Collections.sort(enumValues);

                mysqlType += Strings.join(enumValues, ", ");

                mysqlType += ")";
                return mysqlType;
            }

            if (field.getMax() == field.getMin() && field.getMin() <= CHAR_MAX_CHARS)
                return "CHAR (" + field.getMin() + ")";

            // NOTE: "BLOB and TEXT columns cannot have DEFAULT values"

            final long CHAR_SIZE = MAX_BYTES_UTF8MB4;
            long minLen = field.getMin() * CHAR_SIZE;
            long maxLen = field.getMax() * CHAR_SIZE;

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
                throw new IllegalArgumentException("The maximum number of bytes that strings can hold is "
                        + LONG_MAX + ". Given: " + field.getMax() + ", as number of characters, where each one takes "
                        + CHAR_SIZE + " bytes (" + field.getMax() + " * " + CHAR_SIZE + " = "
                        + field.getMax() * CHAR_SIZE + ").");
        } else if (fieldType.equals(Types.BOOL))
            return "BIT(1)";
        else if (fieldType.equals(Types.DATETIME))
            return "DATETIME"; // Reference: http://dev.mysql.com/doc/refman/5.7/en/datetime.html
        else if (fieldType.equals(Types.DATE))
            return "DATE";
        else if (fieldType.equals(Types.TIME))
            return "TIME";
        else if (fieldType.equals(Types.FILE)) {
            // IDEA: accept different units, not only bytes
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
        } else
            throw new RuntimeException("The type provided for the field " + field.getName() + " is not valid. Given: " +
                    fieldType + ". Valid primitive types are: " + Strings.join(Types.ALL, ", "));
    }
}
