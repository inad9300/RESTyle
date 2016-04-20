package es.berry.restyle.specification;

import es.berry.restyle.specification.generated.Resource;

/**
 * Resolves resource inheritance.
 */
public class ResourceResolver {

    public static void extend(Resource child, Resource parent) {
        if (parent.getName() != null) child.setName(parent.getName());
        if (parent.getDisplayName() != null) child.setDisplayName(parent.getDisplayName());
        if (parent.getDescription() != null) child.setDescription(parent.getDescription());
        // ...
        // TODO
    }
}
