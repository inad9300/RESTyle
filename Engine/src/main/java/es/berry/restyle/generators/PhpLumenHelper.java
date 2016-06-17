package es.berry.restyle.generators;

import es.berry.restyle.specification.generated.Field;
import es.berry.restyle.specification.generated.Resource;
import es.berry.restyle.utils.Numbers;
import es.berry.restyle.utils.Strings;

import java.util.LinkedList;
import java.util.List;

/**
 * Helper class for the PhpLumen one, containing generic information relative to the Lumen (and Laravel) framework.
 */
public class PhpLumenHelper {

    // public static final String FILE_PREFIX = "<?php\n\n";

    public static String getClassName(Resource res) {
        return Strings.studly(res.getName());
    }

    public static String getClassNamePlural(Resource res) {
        return Strings.studly(res.getPlural());
    }

    /**
     * Convert between the types defined by the specification, and those supported by Laravel's casting feature.
     * <p>
     * Check documentation at https://laravel.com/docs/5.2/eloquent-mutators#attribute-casting
     */
    public static String getCastType(Field field) {
        switch (field.getType()) {
            case BOOL:
                return "boolean";
            case FLOAT:
            case DECIMAL:
                return "float";
            case INT:
                return "integer";
            case STRING:
                return "string";
            // Don't cast (Collection::jsonSerialize() will fail due to zero-valued timestamps). See
            // https://laracasts.com/discuss/channels/general-discussion/collectionjsonserialize-fails-on-carbon-date-made-from-postgresql-timestamp?page=1
            case DATE:
                // return "date";
            case DATETIME:
                // return "datetime";
            case FILE:
            case TIME:
                // Don't cast (there is no type to cast to)
        }
        return null;
    }

    /**
     * Produce a Laravel's validation rule based on the restrictions defined for a field in the specification.
     */
    public static String generateValidationRule(Field field, String tableName) {
        // NOTE: rules must be build as PHP arrays to avoid problems. See:
        // http://stackoverflow.com/questions/32810385/laravel-preg-match-no-ending-delimiter-found

        List<String> rules = new LinkedList<>();

        if (field.getRequired())
            rules.add("required");

        switch (field.getType()) {
            case BOOL:
                rules.add("boolean");
                break;
            case INT:
                rules.add("integer");
                break;
            case STRING:
                rules.add("string");
                break;
            case DECIMAL:
            case FLOAT:
                rules.add("numeric");
                break;
            case DATE:
            case DATETIME:
                rules.add("date");
            case TIME:
            case FILE:
                break;
        }

        if (field.getPattern() != null)
            rules.add("regex:" + Strings.surround(field.getPattern(), "/") + Strings.join(field.getPatternOptions(), ""));

        if (field.getEnum() != null && field.getEnum().size() > 0) {
            String prefix = "in:";

            if (field.getType().equals(Field.Type.FILE))
                prefix = "mimetypes:";

            rules.add(prefix + Strings.join(",", field.getEnum()));
        }

        if (field.getUnique())
            rules.add("unique:" + tableName);

        if (field.getType().equals(Field.Type.INT) || field.getType().equals(Field.Type.STRING)) {
            if (field.getMin() != null) rules.add("min:" + field.getMin());
            if (field.getMax() != null) rules.add("max:" + field.getMax());
        } else if (field.getType().equals(Field.Type.FILE)) {
            final long min = Numbers.getLong(field.getMin());
            final long max = Numbers.getLong(field.getMax());

            // The spec. is in bytes, but Lumen assumes KB, reason why we divide by 1024
            if (field.getMin() != null) rules.add("min:" + min / 1024);
            if (field.getMax() != null) rules.add("max:" + max / 1024);
        }

        for (int i = 0; i < rules.size(); i++)
            rules.set(i, Strings.surround(rules.get(i), "'"));

        return Strings.join(rules, ", ");
    }
}
