package es.berry.restyle.specification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.berry.restyle.exceptions.SpecException;

import java.util.Iterator;

/**
 * Responsible of resolving the types of the resources' fields, when they are defined based on another one written in
 * the top-level "types" property.
 */
public class FieldsTypeResolver {

    // Constants to keys referencing the specification
    final private String TYPES = "types";
    final private String TYPE_NAME = "name";
    final private String RESOURCES = "resources";
    final private String RESOURCE_FIELDS = "fields";
    final private String FIELD_TYPE = "type";

    final private JsonNode spec;

    public FieldsTypeResolver(JsonNode spec) {
        this.spec = spec;
    }

    /**
     * Find a type in the "types" array at the root of the specification, given its name ("name" property, referred from
     * the "type" property on the "resources"'s "fields").
     */
    private JsonNode findType(String typeName) {
        JsonNode returnType = null;

        for (JsonNode customType : spec.get(TYPES))
            if (typeName.equals(customType.get(TYPE_NAME).asText())) {
                if (returnType != null)
                    throw new SpecException("The type \"" + typeName + "\" was defined twice in \"types\".");

                returnType = customType;
            }

        if (returnType == null)
            throw new SpecException("The type \"" + typeName + "\" was used but not declared in \"types\".");

        return returnType;
    }

    /**
     * Recursively resolve one particular field base on the values of the root-level "types" field.
     */
    private ObjectNode resolveOne(ObjectNode field) {
        JsonNode type = field.get(FIELD_TYPE);
        assert type != null;

        if (type.isNull())
            throw new NullPointerException("No field can exist without a type");
        else if (Types.ALL.contains(type.asText()))
            return field;
        else {
            JsonNode parentType = findType(type.asText());

            if (parentType.get(TYPE_NAME).asText().equals(parentType.get(FIELD_TYPE).asText()))
                throw new SpecException("A circular dependency exists in type \"" + parentType.get(TYPE_NAME).asText()
                        + "\".");

            // Override the type to allow recursion
            field.put(FIELD_TYPE, parentType.get(FIELD_TYPE).asText());

            // Get all the other properties from the parent, if not already present in the child (child's properties
            // override the parent's ones)
            Iterator<String> fieldProperties = parentType.fieldNames();
            while (fieldProperties.hasNext()) {
                String key = fieldProperties.next();
                JsonNode parentValue = parentType.get(key);

                if (!field.has(key) || field.get(key).isNull())
                    field.set(key, parentValue);
            }

            return resolveOne(field);
        }
    }

    /**
     * Apply the type resolution for each field of each resource.
     */
    public void resolve() {
        for (JsonNode resource : spec.get(RESOURCES)) {
            ArrayNode resolvedFields = SpecObjectMapper.getInstance().createArrayNode();

            for (JsonNode field : resource.get(RESOURCE_FIELDS))
                resolvedFields.add(resolveOne((ObjectNode) field));

            ((ObjectNode) resource).putArray(RESOURCE_FIELDS).addAll(resolvedFields);
        }
    }
}
