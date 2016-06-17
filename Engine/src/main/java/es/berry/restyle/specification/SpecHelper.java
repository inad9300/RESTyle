package es.berry.restyle.specification;

import com.fasterxml.jackson.databind.JsonNode;
import es.berry.restyle.exceptions.SpecException;
import es.berry.restyle.specification.generated.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Set of handy methods ready to be used by the plugins.
 */
final public class SpecHelper {

    /**
     * Try to find a resource inside the spec, by its name.
     */
    public static Resource findResourceByName(Spec spec, String name) {
        for (Resource res : spec.getResources())
            if (res.getName().equals(name))
                return res;

        return null;
    }

    /**
     * Overload findResourceByName to work with the specification on its raw form, directly as a JsonNode.
     */
    public static JsonNode findResourceByName(JsonNode specNode, String name) {
        for (JsonNode res : specNode.get("resources"))
            if (res.get("name").asText("").equals(name))
                return res;

        return null;
    }

    /**
     * Try to find a relation inside a resource, by its name.
     */
    public static Relation findRelationByName(Resource res, String relName) {
        for (Relation rel : res.getRelations())
            if (rel.getWith().equals(relName))
                return rel;

        return null;
    }

    /**
     * Overload findRelationByName to work with the specification on its raw form, directly as a JsonNode.
     */
    public static JsonNode findRelationByName(JsonNode res, String relName) {
        for (JsonNode rel : res.get("relations"))
            if (rel.get("with").asText("").equals(relName))
                return rel;

        return null;
    }

    /**
     * Try to find a field inside a resource, by its name.
     */
    public static Field findFieldByName(Resource res, String fieldName) {
        for (Field f : res.getFields())
            if (f.getName().equals(fieldName))
                return f;

        return null;
    }

    /**
     * Overload findFieldByName to work with the specification on its raw form, directly as a JsonNode.
     */
    public static JsonNode findFieldByName(JsonNode res, String fieldName) {
        for (JsonNode f : res.get("fields"))
            if (f.get("name").asText("").equals(fieldName))
                return f;

        return null;
    }

    /**
     * Check whether a resource is related with another (of the specified type, if given) or not.
     */
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

    /**
     * Overload resourceContainsRelation to pass no type by default.
     */
    public static boolean resourceContainsRelation(Resource res, String relName) {
        return resourceContainsRelation(res, relName, null);
    }

    /**
     * Determines whether or not a many-to-many relationship exists between two resources.
     */
    public static boolean haveManyToManyRelationship(Resource resA, Resource resB) {
        final Relation resAinB = findRelationByName(resB, resA.getName());
        final Relation resBinA = findRelationByName(resA, resB.getName());

        return resAinB != null && resBinA != null;
    }

    /**
     * Determines if a relationship of the type "belongs to" exists between two resources. Will return true if the
     * first resource given belongs to the second one.
     * <p>
     * More concretely, a resource A belongs to a resource B if B "hasOne" A, or B "hasMany" A and not many-to-many
     * relationship exists between the two. Put in other words, a resource A would belong to a resource B if A
     * participates in a 1:1 or 1:M relationship, and is in the side that does not define the relationship in the
     * specification.
     */
    public static boolean belongsTo(Resource resA, Resource resB) {
        final Relation resAinB = findRelationByName(resB, resA.getName());

        return resAinB != null && !haveManyToManyRelationship(resA, resB);
    }

    /**
     * Try to find a role in the root-level "roles" array, by name.
     */
    public static Role findRoleByName(Spec spec, String roleName) {
        for (Role role : spec.getRoles())
            if (roleName.equals(role.getName()))
                return role;

        return null;
    }

    /**
     * Find the role marked as guest in the "roles" array.
     */
    public static Role findGuestRole(Spec spec) {
        for (Role role : spec.getRoles())
            if (role.getIsGuest() != null && role.getIsGuest())
                return role;

        return null;
    }

    /**
     * Find the role marked as admin in the "roles" array, if any.
     */
    public static Role findAdminRole(Spec spec) {
        for (Role role : spec.getRoles())
            if (role.getIsAdmin() != null && role.getIsAdmin())
                return role;

        return null;
    }

    /**
     * Check if there are reflexive relations in the given specification and, if that is the case, throw an exception
     * indicating that they are not supported.
     */
    public static void unsupportedReflexiveRelations(Spec spec) {
        for (Resource res : spec.getResources())
            for (Relation rel : res.getRelations())
                if (res.getName().equals(rel.getWith()))
                    throw new SpecException("Reflexive relationships are not supported by this plugin. Found " +
                            res.getName() + " related with itself.");
    }

    public static boolean roleCanRead(Resource res, String roleName) {
        return roleCan(res, roleName, "read");
    }

    public static boolean roleCanCreate(Resource res, String roleName) {
        return roleCan(res, roleName, "create");
    }

    public static boolean roleCanUpdate(Resource res, String roleName) {
        return roleCan(res, roleName, "update");
    }

    public static boolean roleCanDelete(Resource res, String roleName) {
        return roleCan(res, roleName, "delete");
    }

    /**
     * Check whether a role has direct (thus disregarding role inheritance) permissions to perform a certain type of
     * operation over the given resource.
     * <p>
     * NOTE: the ACL list can be either an ArrayList<String> or a LinkedHashMap<String, ArrayList<String>>.
     */
    private static boolean roleCan(Resource res, String roleName, String roleMapKey) {
        if (res == null || roleName == null)
            throw new NullPointerException("Both the resource and the role name are mandatory to calculate the permissions");

        if (res.getAcl() == null)
            return true;

        if (res.getAcl() instanceof ArrayList) {
            for (Object role : (ArrayList) res.getAcl())
                if (role instanceof String && role.equals(roleName))
                    return true;
        } else if (res.getAcl() instanceof LinkedHashMap) {
            LinkedHashMap<String, ArrayList<String>> acl = (LinkedHashMap) res.getAcl();
            Object readList = acl.get(roleMapKey);
            if (readList instanceof ArrayList)
                for (Object role : (ArrayList) readList)
                    if (role instanceof String && role.equals(roleName))
                        return true;
        }

        return false;
    }
}
