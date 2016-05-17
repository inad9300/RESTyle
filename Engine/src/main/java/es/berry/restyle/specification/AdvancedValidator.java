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

    private final Spec spec;

    public AdvancedValidator(Spec spec) {
        this.spec = spec;
    }

    public void validate() {
        relationshipsValidation();
        indexesValidation();
        rolesValidation();
        minMaxValidation();
        isUserDuplicatedValidation();
        idGenerationValidation();
    }

    /**
     * Ensure that only one resource is marked as user.
     */
    private void isUserDuplicatedValidation() {
        List<String> resourceNamesWithAsUser = new LinkedList<>();

        for (Resource res : spec.getResources())
            if (res.getIsUser())
                resourceNamesWithAsUser.add(res.getName());

        if (resourceNamesWithAsUser.size() > 1)
            throw new SpecException("Only one resource can be the \"isUser\" attribute set to true. These resources "
                    + "are currently marked that way: " + Strings.join(resourceNamesWithAsUser, ", "));
    }

    /**
     * Check the values of the "min" and "max" properties of the fields, to see if their types make sense.
     */
    private void minMaxValidation() {
        for (Resource res : spec.getResources())
            for (Field f : res.getFields()) {
                if (f.getMin() == null && f.getMax() == null)
                    continue;

                if (Types.MIN_MAX_INT.contains(f.getType().toString())) {
                    if (f.getMin() != null && !isNumber(f.getMin()))
                        throw new SpecException("The minimum of fields of type "
                                + f.getType() + " must be a number. Check the " + f.getName() + " field");

                    if (f.getMax() != null && !isNumber(f.getMax()))
                        throw new SpecException("The maximum of fields of type "
                                + f.getType() + " must be a number. Check the " + f.getName() + " field");
                }

                if (Types.MIN_MAX_STRING.contains(f.getType().toString())) {
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
                        case DATE:
                            if ((f.getMin() != null && !f.getMin().toString().matches(dateRegex)) ||
                                    (f.getMax() != null && !f.getMax().toString().matches(dateRegex)))
                                throw new SpecException("The min and max properties of field " + f.getName()
                                        + " must follow the ISO 8601 rules for the date part");
                            break;
                        case TIME:
                            if ((f.getMin() != null && !f.getMin().toString().matches(timeRegex)) ||
                                    (f.getMax() != null && !f.getMax().toString().matches(timeRegex)))
                                throw new SpecException("The min and max properties of field " + f.getName()
                                        + " must follow the ISO 8601 rules for the time part");
                            break;
                        case DATETIME:
                            if ((f.getMin() != null && !f.getMin().toString().matches(dateTimeRegex)) ||
                                    (f.getMax() != null && !f.getMax().toString().matches(dateTimeRegex)))
                                throw new SpecException("The min and max properties of field " + f.getName()
                                        + " must follow the ISO 8601 rules for the full date");
                            break;
                    }
                }
            }
    }

    /**
     * Helper function to determine if a generic Object is a Number.
     * <p>
     * True if it is one of: AtomicInteger, AtomicLong, BigDecimal, BigInteger, Byte, Double, Float, Integer, Long,
     * Short -- or any class inheriting from those.
     */
    private boolean isNumber(Object o) {
        return o != null && Number.class.isAssignableFrom(o.getClass());
    }

    /**
     * Helper function to determine if a generic Object is a String.
     */
    private boolean isString(Object o) {
        return o != null && o instanceof String;
    }

    /**
     * If id injection is enabled, no other field should be marked as auto incrementable.
     */
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

    /**
     * Ensure that no resource is related with another one that is not defined. Good to catch spelling mistakes.
     */
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

    /**
     * Check the shape of the indexes defined. Example of a valid index definition:
     * ["-title", "year", "+lastModified"]
     */
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

    /**
     * Ensure that there is a guest role, which is compulsory; and that the inheritance chain built is correct.
     */
    private void rolesValidation() {
        List<String> roleNames = new ArrayList<>();
        boolean thereIsGuestRole = false;

        for (Role role : spec.getRoles()) {
            roleNames.add(role.getName());
            if (role.getIsGuest())
                thereIsGuestRole = true;
        }

        if (!thereIsGuestRole)
            throw new SpecException("No guest role was provided. It is mandatory to specify one.");

        for (Role role : spec.getRoles())
            if (role.getIsA() != null && !roleNames.contains(role.getIsA()))
                throw new SpecException("The role " + role.getName() + " pretends to inherit from a nonexistent one: "
                        + role.getIsA());
    }
}
