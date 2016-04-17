package es.berry.restyle.generators;

import es.berry.restyle.specification.Types;
import es.berry.restyle.specification.generated.Field;
import es.berry.restyle.utils.Strings;

import java.math.BigInteger;
import java.util.*;

public class MysqlHelper {

    public final static String ISO_8601_UTC_TIME = "'%TZ'";
    public final static String ISO_8601_UTC_DATE = "'%Y-%m-%d'";
    public final static String ISO_8601_UTC_FULL = "'%Y-%m-%dT%TZ'";

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

    public static String getCollation(String charset) {
        if ("utf8".equals(charset) || "utf8mb4".equals(charset))
            return charset + "_unicode_ci";

        return null;
    }

    public static String getTypeModifier(Field field) {
        List<String> modifiers = new ArrayList<String>();

        if (field.getRequired())
            modifiers.add("NOT NULL");
        else
            modifiers.add("DEFAULT NULL");

        final String SINGLE_QUOTE = "'";

        if (field.getDefault() != null) {
            // FIXME: does this work for times and dates?
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

        if (!Strings.isEmpty(field.getDescription()))
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
            if (field.getMax() == null)
                field.setMax((long) S_INT_MAX);

            final long min = (long) field.getMin();
            final long max = (long) field.getMax();

            if (min < 0) { // Signed
                if (min >= S_TINY_MIN && max <= S_TINY_MAX)
                    return "TINYINT";
                else if (min >= S_SMALL_MIN && max <= S_SMALL_MAX)
                    return "SMALLINT";
                else if (min >= S_MEDIUM_MIN && max <= S_MEDIUM_MAX)
                    return "MEDIUMINT";
                else if (min >= S_INT_MIN && max <= S_INT_MAX)
                    return "INT";
                else if (min >= S_BIG_MIN && max <= S_BIG_MAX)
                    return "BIGINT";
                else
                    throw new IllegalArgumentException(
                            "The int values must be between " + S_BIG_MIN + " and " + S_BIG_MAX
                                    + ". Given minimum and maximum: " + field.getMin() + " and " + field.getMax() + ".");
            } else { // Unsigned
                String mysqlType;

                if (max <= U_TINY_MAX)
                    mysqlType = "TINYINT";
                else if (max <= U_SMALL_MAX)
                    mysqlType = "SMALLINT";
                else if (max <= U_MEDIUM_MAX)
                    mysqlType = "MEDIUMINT";
                else if (max <= U_INT_MAX)
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


            if ((double) field.getMin() >= 0)
                mysqlType += " UNSIGNED";

            return mysqlType;
        } else if (fieldType.equals(Types.STRING)) {
            if (field.getEnum() != null && field.getEnum().size() > 0) {
                String mysqlType = "ENUM(";

                List<String> enumValues = new ArrayList<>();
                for (Object val : field.getEnum())
                    if (val instanceof String)
                        enumValues.add(Strings.surround((String) val, "'"));

                // NOTE: from http://dev.mysql.com/doc/refman/5.7/en/enum.html: "To prevent unexpected results when
                // using the ORDER BY clause on an ENUM column, (...) Specify the ENUM list in alphabetic order"
                Collections.sort(enumValues);

                mysqlType += Strings.join(enumValues, ", ");

                mysqlType += ")";
                return mysqlType;
            }

            if (field.getMin() == null)
                field.setMin((long) 0);

            if (field.getMax() == null)
                field.setMax((long) VARCHAR_MAX_BYTES);

            if (field.getMax().equals(field.getMin()) && (int) field.getMin() <= CHAR_MAX_CHARS)
                return "CHAR (" + field.getMin() + ")";

            // NOTE: "BLOB and TEXT columns cannot have DEFAULT values"

            final int CHAR_SIZE = MAX_BYTES_UTF8MB4;

            if (field.getMax() instanceof Integer)
                field.setMax(((Integer) field.getMax()).longValue());

            final long maxBytes = (long) field.getMax() * CHAR_SIZE;

            // NOTE: by now, TEXT_MAX = VARCHAR_MAX_BYTES = 65535 bytes, so TINYTEXT and TEXT will not be chosen
            if (maxBytes <= VARCHAR_MAX_BYTES)
                return "VARCHAR(" + field.getMax() + ")";
            else if (maxBytes < TINY_MAX)
                return "TINYTEXT";
            else if (maxBytes < TEXT_MAX)
                return "TEXT";
            else if (maxBytes < MEDIUM_MAX)
                return "MEDIUMTEXT";
            else if (maxBytes < LONG_MAX)
                return "LONGTEXT";
            else
                throw new IllegalArgumentException("The maximum number of bytes that strings can hold is "
                        + LONG_MAX + ". Given: " + field.getMax() + ", as number of characters, where each one takes "
                        + CHAR_SIZE + " bytes (" + field.getMax() + " * " + CHAR_SIZE + " = "
                        + maxBytes + ").");
        } else if (fieldType.equals(Types.BOOL))
            return "BIT(1)";
        else if (fieldType.equals(Types.DATETIME))
            return "DATETIME"; // Reference: http://dev.mysql.com/doc/refman/5.7/en/datetime.html
        else if (fieldType.equals(Types.DATE))
            return "DATE";
        else if (fieldType.equals(Types.TIME))
            return "TIME";
        else if (fieldType.equals(Types.FILE)) {
            if (field.getMax() == null)
                field.setMax((long) TEXT_MAX);

            final long max = (long) field.getMax();

            // IDEA: accept different units, not only bytes
            if (max <= TINY_MAX)
                return "TINYBLOB";
            else if (max <= TEXT_MAX)
                return "BLOB";
            else if (max <= MEDIUM_MAX)
                return "MEDIUMBLOB";
            else if (max <= LONG_MAX)
                return "LONGBLOB";
            else
                throw new RuntimeException("Sorry, a file that big cannot be stored");
        } else
            throw new RuntimeException("The type provided for the field " + field.getName() + " is not valid. Given: " +
                    fieldType + ". Valid primitive types are: " + Strings.join(Types.ALL, ", "));
    }
}
