package es.berry.restyle.core;

import es.berry.restyle.exceptions.SpecException;
import es.berry.restyle.specification.Field;
import es.berry.restyle.specification.Relation;
import es.berry.restyle.specification.Resource;
import es.berry.restyle.specification.Spec;

import java.util.ArrayList;
import java.util.List;

public class SpecAdvanceValidator {

    private final Spec spec;

    public SpecAdvanceValidator(Spec spec) {
        this.spec = spec;
    }

    public void validate() {
        relationshipsValidation();
        indexesValidation();
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
}
