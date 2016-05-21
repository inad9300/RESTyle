package es.berry.restyle.generators;

import com.fasterxml.jackson.databind.node.ObjectNode;
import es.berry.restyle.specification.Types;
import es.berry.restyle.specification.generated.Field;
import es.berry.restyle.utils.Numbers;
import es.berry.restyle.utils.Strings;

import java.math.BigInteger;
import java.util.*;

/**
 * Class to hold some generic information regarding MySQL, that potentially other plugins with similar targets may find
 * useful.
 */
public class MysqlHelper {

    public final static String ISO_8601_UTC_TIME = "'%TZ'";
    public final static String ISO_8601_UTC_DATE = "'%Y-%m-%d'";
    public final static String ISO_8601_UTC_FULL = "'%Y-%m-%dT%TZ'";

//    public final static class InheritanceStrategies {
//        public final static String SINGLE_TABLE = "singleTable";
//        public final static String TABLE_PER_CONCRETE = "tablePerConcreteClass";
//        public final static String TABLE_PER_CLASS = "tablePerClass";
//    }

    /**
     * Transforms the name of an encoding given on its "standard" form, to the one preferred by MySQL.
     * <p>
     * NOTE: to list the available character sets and their default collations use the "show character set;" statement
     */
    public static String adaptEncodingName(String name) {
        final String UTF_8 = "utf-8";
        final Set<String> validCharsets = new HashSet<>(Collections.singletonList(UTF_8));

        name = name.toLowerCase();

        if (name.equals(UTF_8))
            return "utf8mb4";

        throw new IllegalArgumentException("The specified character set is not supported by this plugin. Choose one of "
                + Strings.join(validCharsets, ", ").toUpperCase());
    }

    /**
     * Given a character set, return a collation by default.
     * <p>
     * NOTE: in general, *_general_ci is more performant, but less accurate, while *_unicode_ci properly implements the
     * unicode sorting rules, being a bit less efficient
     */
    public static String getCollation(String charset) {
        if ("utf8".equals(charset) || "utf8mb4".equals(charset))
            return charset + "_unicode_ci";

        return null;
    }

    /**
     * Convert from the values expected in the specification, to the ones preferred by MySQL.
     */
    public static String transformReferencialActions(String name) {
        switch (name) {
            case "restrict":
                return "RESTRICT";
            case "noAction":
                return "NO ACTION";
            case "cascade":
                return "CASCADE";
            case "setNull":
                return "SET NULL";
            case "setDefault":
                return "SET DEFAULT";
        }
        return name.toUpperCase();
    }

    /**
     * Determines whether a field needs quotes when using a "DEFAULT" on it, based on its type and value.
     */
    public static boolean typeNeedsQuotesForDefaultValue(Field.Type t, Object defVal) {
        switch (t) {
            case STRING:
                return true;
            case DATE:
            case DATETIME:
            case TIME:
                // Quick check to see if the value is an actual date or time. Otherwise, we assume some kind of constant
                // or function (e.g. "NOW()"). Notice that the value used in the check is one that no constant or
                // function is allowed to have, and that the values must have if they effectively were dates or times.
                final String defValStr = (String) defVal;
                if (t.equals(Field.Type.TIME)) {
                    if (defValStr.contains(":"))
                        return true;
                } else if (defValStr.contains("-"))
                    return true;

        }
        return false;
    }

    /**
     * Return a string with all the modifiers that can be applied to a MySQL column, based on the restrictions defined
     * for the corresponding field in the specification.
     */
    public static String getTypeModifier(Field field, ObjectNode rawField) {
        List<String> modifiers = new ArrayList<>();

        if (field.getRequired())
            modifiers.add("NOT NULL");
        else
            modifiers.add("DEFAULT NULL");

        final String SINGLE_QUOTE = "'";

        if (field.getDefault() != null) {
            final String defStr = field.getDefault().toString();
            final boolean needsQuotes = typeNeedsQuotesForDefaultValue(field.getType(), field.getDefault())
                    && !defStr.startsWith(SINGLE_QUOTE)
                    && !defStr.endsWith(SINGLE_QUOTE);
            modifiers.add("DEFAULT " + (needsQuotes ? Strings.surround(defStr, SINGLE_QUOTE) : field.getDefault()));
        }

        if (!Strings.isEmpty(field.getOnUpdate()))
            modifiers.add("ON UPDATE " + field.getOnUpdate());

        if (field.getAutoIncrement())
            modifiers.add("AUTO_INCREMENT");

        if (field.getUnique())
            modifiers.add("UNIQUE");

        if (!Strings.isEmpty(field.getDescription()))
            modifiers.add("COMMENT " + Strings.surround(field.getDescription(), SINGLE_QUOTE));

        return Strings.join(modifiers, " ");
    }

    /**
     * Convert between the types supported by the specification for fields and those allowed in MySQL for columns,
     * trying to find the best fit specially looking into the limits defined by the "min" and "max" attributes of the
     * field.
     */
    public static String getType(Field field, ObjectNode rawField) {
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

        // final int MAX_BYTES_UTF8 = 3;
        final int MAX_BYTES_UTF8MB4 = 4;

        final int CHAR_MAX_CHARS = 255;
        final int VARCHAR_MAX_BYTES = 65535;

        String fieldType = field.getType().toString();

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
                else {
                    // NOTE: the tool used to automatically translate JSON Schemas into Java classes goes up to the
                    // "long" type for integers, but sometimes something bigger is needed
                    final BigInteger bigMin = new BigInteger(rawField.get("min").asText());
                    final BigInteger bigMax = new BigInteger(rawField.get("max").asText());

                    if (bigMin.compareTo(new BigInteger(Long.toString(S_BIG_MIN))) >= 0 &&
                            bigMax.compareTo(new BigInteger(Long.toString(S_BIG_MAX))) <= 0)
                        return "BIGINT";
                    else
                        throw new IllegalArgumentException(
                                "The int values must be between " + S_BIG_MIN + " and " + S_BIG_MAX
                                        + ". Given minimum and maximum: " + field.getMin() + " and " + field.getMax()
                                        + ".");
                }
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
            String mysqlType = fieldType.equals(Types.FLOAT) ? "DOUBLE" : "DECIMAL";

            // If not specified, calculate the precision based on the minimum and maximum values
            if (field.getPrecision() == null) {
                final String minStr = Double.toString((double) field.getMin());
                final String maxStr = Double.toString((double) field.getMax());

                final long digitsMin = minStr.length();
                final long digitsMax = maxStr.length();

                final long decimalsMin = minStr.split("\\.")[1].length();
                final long decimalsMax = maxStr.split("\\.")[1].length();

                field.setPrecision(Arrays.asList(
                        Math.max(digitsMin, digitsMax),
                        Math.max(decimalsMin, decimalsMax)
                ));
            }

            // For reference, see http://dev.mysql.com/doc/refman/5.7/en/numeric-type-overview.html
            if (field.getPrecision() != null && field.getPrecision().size() > 0) {
                assert field.getPrecision().size() == 2;

                // Decimal maximums
                int MAX_DIGITS = 65;
                int MAX_DECIMALS = 30;
                String typeName = "decimal";

                if (fieldType.equals(Types.FLOAT)) {
                    // Double maximums, based on the feedback from the MySQL console when introducing clearly-out-of-
                    // the-limits values, since the documentation is not clear at all about this topic (theoretically,
                    // there are 64 bits in total (supposedly 63 if signed), and up to 53 bits for precision)
                    MAX_DIGITS = 255;
                    // MAX_DECIMALS = 30; // Same as before
                    typeName = "float";
                }

                if (field.getPrecision().get(0) > MAX_DIGITS)
                    throw new IllegalArgumentException("MySQL does not allow " + typeName + " precisions bigger than "
                            + MAX_DIGITS);

                if (field.getPrecision().get(1) > MAX_DECIMALS)
                    throw new IllegalArgumentException("MySQL does not allow " + typeName + "s to have more than "
                            + MAX_DECIMALS + " digits to the right of the decimal point.");

                if (field.getPrecision().get(1) > field.getPrecision().get(0))
                    throw new IllegalArgumentException("The number of digits for a " + typeName + " cannot be bigger "
                            + "than the total number of them.");

                mysqlType += " (" + field.getPrecision().get(0) + ", " + field.getPrecision().get(1) + ")";
            }

            if ((long) field.getMin() >= 0)
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

            final long max = Numbers.getLong(field.getMax());

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
            throw new RuntimeException("The type provided for the field " + field.getName() + " is not valid. Given: "
                    + fieldType + ". Valid primitive types are: " + Strings.join(Types.ALL, ", "));
    }
}
