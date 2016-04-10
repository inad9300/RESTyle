package es.berry.restyle.specification;

import es.berry.restyle.specification.generated.Relation;
import es.berry.restyle.specification.generated.Resource;
import es.berry.restyle.specification.generated.Spec;

final public class SpecHelper {

    public static Resource findResourceByName(Spec spec, String name) {
        for (Resource res : spec.getResources())
            if (res.getName().equals(name))
                return res;

        return null;
    }

    // Try to find a relation inside a resource
    public static Relation findRelationByName(Resource res, String relName) {
        for (Relation rel : res.getRelations())
            if (rel.getWith().equals(relName))
                return rel;

        return null;
    }

    public static boolean resourceContainsRelation(Resource res, String relName) {
        return resourceContainsRelation(res, relName, null);
    }

    public static boolean resourceContainsRelation(Resource res, String relName, String type) {
        for (Relation rel : res.getRelations())
            if (rel.getWith().equals(relName)) {
                if (type == null)
                    return true;
                else if (type.equals(rel.getType().toString()))
                    return true;
            }

        return false;
    }
}
