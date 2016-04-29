package es.berry.restyle.generators;

import es.berry.restyle.specification.Types;
import es.berry.restyle.specification.generated.Field;
import es.berry.restyle.specification.generated.Resource;
import es.berry.restyle.utils.Strings;

import java.util.*;

public class PhpLumenHelper {

    public static final String FILE_PREFIX = "<?php\n\n";

    // IDEA: transform snake_case to CamelCase
    public static String getClassName(Resource res) {
        return Strings.ucFirst(res.getName(), true);
    }

    public static String getClassNamePlural(Resource res) {
        return Strings.ucFirst(res.getPlural(), true);
    }

    // Check documentation at https://laravel.com/docs/5.2/eloquent-mutators#attribute-casting
    public static String getCastType(Field field) {
        switch (field.getType()) {
            case Types.BOOL:
                return "boolean";
            case Types.DATE:
                return "date";
            case Types.DATETIME:
                return "datetime";
            case Types.FLOAT:
            case Types.DECIMAL:
                return "float";
            case Types.INT:
                return "integer";
            case Types.STRING:
                return "string";
            // Don't cast (there is no type to cast to)
            case Types.FILE:
            case Types.TIME:
        }
        return null;
    }

    public static String generateValidationRule(Field field, String tableName) {
        List<String> rules = new LinkedList<>();

        if (field.getRequired())
            rules.add("required");

        switch (field.getType()) {
            case Types.BOOL:
                rules.add("boolean");
                break;
            case Types.INT:
                rules.add("integer");
                break;
            case Types.STRING:
                rules.add("string");
                break;
            case Types.DECIMAL:
            case Types.FLOAT:
                rules.add("numeric");
                break;
            case Types.DATE:
                // TODO
                // rules.add("date_format:");
            case Types.TIME:
            case Types.DATETIME:
            case Types.FILE:
                break;
        }

        if (field.getPattern() != null)
            rules.add("regex:" + field.getPattern());

        if (field.getEnum() != null && field.getEnum().size() > 0)
            rules.add("in:" + Strings.join(",", field.getEnum()));

        if (field.getUnique())
            rules.add("unique:" + tableName);

        // FIXME: file size must be considered on KB by default!
        if (Arrays.asList(Types.INT, Types.STRING, Types.FILE).contains(field.getType())) {
            if (field.getMin() != null)
                rules.add("min:" + field.getMin());
            if (field.getMax() != null)
                rules.add("max:" + field.getMax());
        }

        // IDEA: mime; url, email, ip?

        return Strings.join(rules, "|");
    }
}
