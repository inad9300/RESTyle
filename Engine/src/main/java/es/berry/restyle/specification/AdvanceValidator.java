package es.berry.restyle.specification;

import es.berry.restyle.exceptions.SpecException;
import es.berry.restyle.specification.generated.*;
import es.berry.restyle.utils.Strings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdvanceValidator {

    private final Spec spec;

    public AdvanceValidator(Spec spec) {
        this.spec = spec;
    }

    public void validate() {
        relationshipsValidation();
        indexesValidation();
        rolesValidation();
        checkIdGenerationConflicts();
    }

    private void checkIdGenerationConflicts() {
        Set<String> autoIncrementalFieldNames = new HashSet<String>();

        for (Resource res : spec.getResources())
            for (Field f : res.getFields())
                if (f.getAutoIncrement())
                    if (res.getIdInjection())
                        throw new SpecException("The resource " + res.getName() + " was marked to have an ID injected. "
                                + "The field " + f.getName() + " cannot be auto incrementable at the same time.");
                    else
                        autoIncrementalFieldNames.add(f.getName());

        if (autoIncrementalFieldNames.size() > 1)
            throw new SpecException("Only one field can be marked as auto incrementable. Currently, these fields are "
                    + "marked: " + Strings.join(autoIncrementalFieldNames, ", "));
    }

    private void relationshipsValidation() {
        List<String> resourceNames = new ArrayList<String>();
        for (Resource res : spec.getResources())
            resourceNames.add(res.getName());

        for (Resource res : spec.getResources()) {
            for (Relation rel : res.getRelations())
                if (!resourceNames.contains(rel.getWith()))
                    throw new SpecException("Trying to relate the resource " + res.getName()
                            + " with a nonexistent one: " + rel.getWith());
        }
    }

    private void indexesValidation() {
        for (Resource res : spec.getResources()) {
            List<String> fieldNames = new ArrayList<String>();
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
        List<String> roleNames = new ArrayList<String>();
        for (Role role : spec.getRoles())
            roleNames.add(role.getName());

        for (Role role : spec.getRoles())
            if (role.getIsA() != null && !roleNames.contains(role.getIsA()))
                throw new SpecException("The role " + role.getName() + " pretends to inherit from a nonexistent one: "
                        + role.getIsA());
    }
}
