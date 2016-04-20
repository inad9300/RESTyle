package es.berry.restyle.specification;

import es.berry.restyle.exceptions.SpecException;
import es.berry.restyle.specification.generated.Field;
import es.berry.restyle.specification.generated.Resource;
import es.berry.restyle.specification.generated.Spec;
import es.berry.restyle.specification.generated.Type;

import java.util.HashSet;
import java.util.Set;

/**
 * Responsible of resolving the types of the resources' fields, when they are defined based on another one written in
 * the top-level "types" property.
 */
public class FieldsTypeResolver {

    final private Spec spec;

    public FieldsTypeResolver(Spec spec) {
        this.spec = spec;
    }

    public Spec getSpec() {
        return this.spec;
    }

    private Type findType(String typeName) {
        Type returnType = null;

        for (Type customType : spec.getTypes()) {
            if (typeName.equals(customType.getName())) {
                if (returnType != null)
                    throw new SpecException("The type \"" + typeName + "\" was defined twice in \"types\".");

                returnType = customType;
            }
        }

        if (returnType == null)
            throw new SpecException("The type \"" + typeName + "\" was used but not declared in \"types\".");

        return returnType;
    }

    private Field resolveOne(Field field) {
        if (Types.ALL.contains(field.getType()))
            return field;
        else {
            Type parentType = findType(field.getType());

            if (parentType.getName().equals(parentType.getType()))
                throw new SpecException("A circular dependency exists in type \"" + parentType.getName() + "\".");

            // Override the type to allow recursion
            field.setType(parentType.getType());

            // Get all the other properties from the parent, if not already present
            if (field.getName() == null) field.setName(parentType.getName());
            if (field.getDescription() == null) field.setDescription(parentType.getDescription());
            if (field.getRequired() == null) field.setRequired(parentType.getRequired());
            if (field.getDefault() == null) field.setDefault(parentType.getDefault());
            if (field.getMin() == null) field.setMin(parentType.getMin());
            if (field.getMax() == null) field.setMax(parentType.getMax());
            if (field.getPrecision() == null) field.setPrecision(parentType.getPrecision());
            if (field.getEnum() == null) field.setEnum(parentType.getEnum());
            if (field.getPattern() == null) field.setPattern(parentType.getPattern());
            if (field.getUnique() == null) field.setUnique(parentType.getUnique());
            if (field.getFilterable() == null) field.setFilterable(parentType.getFilterable());
            if (field.getSortable() == null) field.setSortable(parentType.getSortable());
            if (field.getReadOnly() == null) field.setReadOnly(parentType.getReadOnly());
            if (field.getWriteOnly() == null) field.setWriteOnly(parentType.getWriteOnly());
            if (field.getOnUpdate() == null) field.setOnUpdate(parentType.getOnUpdate());
            if (field.getEncrypted() == null) field.setEncrypted(parentType.getEncrypted());
            if (field.getAutoIncrement() == null) field.setAutoIncrement(parentType.getAutoIncrement());

            return resolveOne(field);
        }
    }

    public FieldsTypeResolver resolve() {
        for (Resource resource : spec.getResources()) {
            Set<Field> resolvedFields = new HashSet<>();
            for (Field field : resource.getFields())
                resolvedFields.add(resolveOne(field));

            resource.setFields(resolvedFields);
        }
        return this;
    }
}
