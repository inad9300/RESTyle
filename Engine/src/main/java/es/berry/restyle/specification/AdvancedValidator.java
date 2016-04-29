package es.berry.restyle.specification;

import es.berry.restyle.exceptions.SpecException;
import es.berry.restyle.specification.generated.*;
import es.berry.restyle.utils.Strings;

import java.util.*;

/**
 * Performs some extra validations beyond the JSON Schema, specially those involving values of one property invalidating
 * values of others.
 */
public class AdvancedValidator {

    // IDEA: reduce all loops over resources to one

    private final Spec spec;

    public AdvancedValidator(Spec spec) {
        this.spec = spec;
    }

    public void validate() {
        relationshipsValidation();
        indexesValidation();
        rolesValidation();
        minMaxValidation();
        isUserDuplicatesValidation();
        idGenerationValidation();
    }

    private void isUserDuplicatesValidation() {
        List<String> resourceNamesWithAsUser = new LinkedList<>();

        for (Resource res : spec.getResources())
            if (res.getIsUser())
                resourceNamesWithAsUser.add(res.getName());

        if (resourceNamesWithAsUser.size() > 1)
            throw new SpecException("Only one resource can be the \"isUser\" attribute set to true. These resources "
                    + "are currently marked that way: " + Strings.join(resourceNamesWithAsUser, ", "));
    }

    private boolean isNumber(Object o) {
        // True if c is one of: AtomicInteger, AtomicLong, BigDecimal, BigInteger, Byte, Double, Float, Integer, Long,
        // Short; or any class inheriting from those.
        return o != null && Number.class.isAssignableFrom(o.getClass());
    }

    private boolean isString(Object o) {
        return o != null && o instanceof String;
    }

    private void minMaxValidation() {
        for (Resource res : spec.getResources())
            for (Field f : res.getFields()) {
                if (f.getMin() == null && f.getMax() == null)
                    continue;

                if (Types.MIN_MAX_INT.contains(f.getType())) {
                    if (f.getMin() != null && !isNumber(f.getMin()))
                        throw new SpecException("The minimum of fields of type "
                                + f.getType() + " must be a number. Check the " + f.getName() + " field");

                    if (f.getMax() != null && !isNumber(f.getMax()))
                        throw new SpecException("The maximum of fields of type "
                                + f.getType() + " must be a number. Check the " + f.getName() + " field");
                }

                if (Types.MIN_MAX_STRING.contains(f.getType())) {
                    if (f.getMin() != null && !isString(f.getMin()))
                        throw new SpecException("The minimum of fields of type "
                                + f.getType() + " must be a string. Check the " + f.getName() + " field");

                    if (f.getMax() != null && !isString(f.getMax()))
                        throw new SpecException("The maximum of fields of type "
                                + f.getType() + " must be a string. Check the " + f.getName() + " field");

                    final String dateRegex = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
                    final String timeRegex = "[0-9]{2}:[0-9]{2}:[0-9]{2}";
                    final String dateTimeRegex = dateRegex + "T" + timeRegex + "Z";

                    switch (f.getType()) {
                        case Types.DATE:
                            if ((f.getMin() != null && !f.getMin().toString().matches(dateRegex)) ||
                                    (f.getMax() != null && !f.getMax().toString().matches(dateRegex)))
                                throw new SpecException("The min and max properties of field " + f.getName()
                                        + " must follow the ISO 8601 rules for the date part");
                            break;
                        case Types.TIME:
                            if ((f.getMin() != null && !f.getMin().toString().matches(timeRegex)) ||
                                    (f.getMax() != null && !f.getMax().toString().matches(timeRegex)))
                                throw new SpecException("The min and max properties of field " + f.getName()
                                        + " must follow the ISO 8601 rules for the time part");
                            break;
                        case Types.DATETIME:
                            if ((f.getMin() != null && !f.getMin().toString().matches(dateTimeRegex)) ||
                                    (f.getMax() != null && !f.getMax().toString().matches(dateTimeRegex)))
                                throw new SpecException("The min and max properties of field " + f.getName()
                                        + " must follow the ISO 8601 rules for the full date");
                            break;
                    }
                }
            }
    }

    private void idGenerationValidation() {
        Set<String> autoIncrementalFieldNames = new HashSet<>();

        for (Resource res : spec.getResources())
            for (Field f : res.getFields())
                if (f.getAutoIncrement())
                    if (res.getIdInjection())
                        throw new SpecException("The resource " + res.getName() + " was marked to have an ID injected. "
                                + "The field " + f.getName() + " cannot be auto incrementing at the same time.");
                    else
                        autoIncrementalFieldNames.add(f.getName());

        if (autoIncrementalFieldNames.size() > 1)
            throw new SpecException("Only one field can be marked as auto incrementing. Currently, these fields are "
                    + "marked: " + Strings.join(autoIncrementalFieldNames, ", "));
    }

    private void relationshipsValidation() {
        List<String> resourceNames = new ArrayList<>();

        for (Resource res : spec.getResources())
            resourceNames.add(res.getName());

        for (Resource res : spec.getResources())
            for (Relation rel : res.getRelations())
                if (!resourceNames.contains(rel.getWith()))
                    throw new SpecException("Trying to relate the resource " + res.getName()
                            + " with a nonexistent one: " + rel.getWith());
    }

    private void indexesValidation() {
        for (Resource res : spec.getResources()) {
            List<String> fieldNames = new ArrayList<>();
            for (Field field : res.getFields())
                fieldNames.add(field.getName());

            for (String idx : res.getIndex()) {
                String fieldToBeIndexed;
                if (idx.startsWith("+") || idx.startsWith("-"))
                    fieldToBeIndexed = idx.substring(1);
                else
                    fieldToBeIndexed = idx;

                if (!fieldNames.contains(fieldToBeIndexed))
                    throw new SpecException("An index was tried to be created over a nonexistent field: "
                            + fieldToBeIndexed);
            }
        }
    }

    private void rolesValidation() {
        List<String> roleNames = new ArrayList<>();

        for (Role role : spec.getRoles())
            roleNames.add(role.getName());

        for (Role role : spec.getRoles())
            if (role.getIsA() != null && !roleNames.contains(role.getIsA()))
                throw new SpecException("The role " + role.getName() + " pretends to inherit from a nonexistent one: "
                        + role.getIsA());
    }
}
