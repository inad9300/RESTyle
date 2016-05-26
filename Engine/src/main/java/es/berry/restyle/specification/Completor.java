package es.berry.restyle.specification;

import es.berry.restyle.exceptions.SpecException;
import es.berry.restyle.specification.generated.*;
import es.berry.restyle.utils.Strings;
import org.atteo.evo.inflector.English;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Completes the specification with default values. A class is created for this specific purpose because the
 * implementations of JSON Schema may disregard the "default" keyword.
 */
final public class Completor {
    // Global
    private final static String DEF_ENCODING = "UTF-8";
    private final static String DEF_TIME_ZONE = "UTC";
    private final static String DEF_DATE_FMT = "ISO-8601";
    private final static boolean DEF_IS_ADMIN = false;
    private final static boolean DEF_IS_GUEST = false;
    private final static int DEF_RATE_LIMIT_REQ_NUM = 0;
    // Resources
    private final static boolean DEF_IS_USER = false;
    private final static boolean DEF_ABSTRACT = false;
    private final static boolean DEF_PAGINABLE = true;
    private static final boolean DEF_ID_INJECTION = true;
    private final static int DEF_MIN_INSTANCES = 0;
    private final static int DEF_MAX_INSTANCES = 0;
    private final static boolean DEF_EXTRA_FIELDS = true;
    // Fields
    private final static long DEF_MIN = 0;
    private final static int SPECIAL_MAX = 0;
    private static final boolean DEF_AUTO_INCREMENT = false;
    private static final boolean DEF_REQUIRED = false;
    private static final boolean DEF_UNIQUE = false;
    private final static boolean DEF_FILTERABLE = true;
    private final static boolean DEF_SORTABLE = true;
    private final static boolean DEF_WRITE_ONLY = false;
    private final static boolean DEF_READ_ONLY = false;
    private final static boolean DEF_ENCRYPTED = false;
    private final static Object DEF_ACL = null;

    final private Spec spec;

    public Completor(Spec spec) {
        this.spec = spec;
    }

    public Spec getSpec() {
        return this.spec;
    }

    /**
     * Return a Collection with all the roles' names. They are returned as simple Objects for convenience -- see use in
     * addUserFields().
     */
    private Set<Object> getUserRoleNames() {
        Set<Object> names = new HashSet<>();

        for (Role role : spec.getRoles())
            names.add(role.getName());

        return names;
    }

    /**
     * Add certain properties to a resource that wants to be considered a user by the system. Should be applied when the
     * resource is marked as user.
     */
    private void addUserFields(Resource res) {
        final List<String> userReservedFieldNames = Arrays.asList("username", "role", "password", "isAdmin");

        Field name = new Field();
        Field role = new Field();
        Field pass = new Field();
        Field isAdmin = new Field();

        // Restrictions similar to those used by most programming languages for variable names
        final String BASIC_NAME_PATTERN = "^[a-zA-Z_$][a-zA-Z_$0-9]*$";

        name.setName("username");
        name.setDescription("Name that uniquely identifies the user in the system");
        name.setType(Field.Type.STRING);
        name.setRequired(true);
        name.setPattern(BASIC_NAME_PATTERN);
        name.setMin(4);
        name.setMax(32);
        name.setDefault("");
        name.setUnique(true);

        final Role guestRole = SpecHelper.findGuestRole(spec);
        if (guestRole != null)
            role.setDefault(guestRole.getName());

        pass.setName("password");
        pass.setDescription("Password the user must use for authentication");
        pass.setType(Field.Type.STRING);
        pass.setRequired(true);
        pass.setMin(8);
        pass.setMax(512);
        pass.setFilterable(false);
        pass.setSortable(false);
        pass.setWriteOnly(true);
        pass.setEncrypted(true);

        role.setName("role");
        role.setDescription("Role of the user");
        role.setType(Field.Type.STRING);
        role.setRequired(true);
        role.setMin(2);
        role.setMax(64);
        role.setEnum(getUserRoleNames());
        role.setReadOnly(true); // Should not be possible to easily choose "your" role

        isAdmin.setName("isAdmin");
        isAdmin.setDescription("Determines whether the user is a super administrator or not");
        isAdmin.setType(Field.Type.BOOL);
        isAdmin.setRequired(true);
        isAdmin.setDefault(false);
        isAdmin.setReadOnly(true); // Should not be possible to create administrators arbitrarily

        Set<Field> fields = res.getFields();

        fields.removeIf(f -> userReservedFieldNames.contains(f.getName()));

        fields.addAll(Arrays.asList(name, role, pass, isAdmin));
        res.setFields(fields);
    }

    /**
     * Add default values to a field's properties.
     */
    private static void addFieldDefaultValues(Field field) {
        if (field.getMin() == null && Types.MIN_MAX_INT.contains(field.getType().toString()))
            field.setMin(DEF_MIN);

        if (field.getMax() != null && field.getMax().equals(SPECIAL_MAX))
            field.setMax(null);

        if (field.getAutoIncrement() == null)
            field.setAutoIncrement(DEF_AUTO_INCREMENT);
        else if (field.getAutoIncrement())
            field.setType(Field.Type.INT);

        if (field.getRequired() == null)
            field.setRequired(DEF_REQUIRED);

        if (field.getUnique() == null)
            field.setUnique(DEF_UNIQUE);

        if (field.getFilterable() == null)
            field.setFilterable(DEF_FILTERABLE);

        if (field.getSortable() == null)
            field.setSortable(DEF_SORTABLE);

        if (field.getWriteOnly() == null)
            field.setWriteOnly(DEF_WRITE_ONLY);

        if (field.getReadOnly() == null)
            field.setReadOnly(DEF_READ_ONLY);

        if (field.getEncrypted() == null)
            field.setEncrypted(DEF_ENCRYPTED);
    }

    /**
     * Recursively resolves one role.
     */
    private Role resolveRole(Role role) {
        // No more levels of inheritance
        if (role == null || role.getIsA() == null || role.getIsAdmin() || role.getIsGuest())
            return role;

        Role parent = SpecHelper.findRoleByName(spec, role.getIsA());
        if (parent == null)
            throw new SpecException("The role " + role.getName() + " is defined as another one that does not exist: "
                    + role.getIsA());

        // Enable recursion
        role.setIsA(parent.getIsA());

        if (role.getRateLimit() == null)
            role.setRateLimit(parent.getRateLimit());
        else if (parent.getRateLimit() != null) {
            if (role.getRateLimit().getNumOfRequests() == null)
                role.getRateLimit().setNumOfRequests(parent.getRateLimit().getNumOfRequests());
            if (role.getRateLimit().getRefreshTime() == null)
                role.getRateLimit().setRefreshTime(parent.getRateLimit().getRefreshTime());
        }
        return resolveRole(role);
    }

    /**
     * Resolve roles' inheritance.
     */
    private Set<Role> resolveRoles(Set<Role> roles) {
        final Set<Role> resolvedRoles = new HashSet<>();

        for (Role role : roles)
            resolvedRoles.add(resolveRole(role));

        return resolvedRoles;
    }

    /**
     * Add default values to the specification.
     */
    public Completor addDefaultValues() {
        if (Strings.isEmpty(spec.getEncoding()))
            spec.setEncoding(DEF_ENCODING);

        if (Strings.isEmpty(spec.getTimeZone()))
            spec.setTimeZone(DEF_TIME_ZONE);

        if (Strings.isEmpty(spec.getDateFormat()))
            spec.setDateFormat(DEF_DATE_FMT);

        for (Role role : spec.getRoles()) {
            if (role.getIsAdmin() == null)
                role.setIsAdmin(DEF_IS_ADMIN);

            if (role.getIsGuest() == null)
                role.setIsGuest(DEF_IS_GUEST);
        }

        spec.setRoles(resolveRoles(spec.getRoles()));

        // Add the defaults only after resolving the possible inheritance on the roles
        for (Role role : spec.getRoles())
            if (role.getRateLimit() != null && role.getRateLimit().getNumOfRequests() == null)
                role.getRateLimit().setNumOfRequests((long) DEF_RATE_LIMIT_REQ_NUM);

        for (Resource resource : spec.getResources()) {
            if (resource.getIsUser() == null)
                resource.setIsUser(DEF_IS_USER);
            else if (resource.getIsUser())
                addUserFields(resource);

            if (Strings.isEmpty(resource.getPlural()))
                // Special case for camel-case notation: pluralize only the last word
                if (resource.getName().contains("_")) {
                    final List<String> words = Arrays.asList(resource.getName().split("_"));
                    final List<String> wordsExceptLast = words.subList(0, words.size() - 1);
                    final String lastWord = words.get(words.size() - 1);
                    resource.setPlural(Strings.join(wordsExceptLast, "_") + "_" + English.plural(lastWord));
                } else {
                    resource.setPlural(English.plural(resource.getName()));
                }

            if (resource.getAbstract() == null)
                resource.setAbstract(DEF_ABSTRACT);

            if (resource.getPaginable() == null)
                resource.setPaginable(DEF_PAGINABLE);

            if (resource.getIdInjection() == null)
                resource.setIdInjection(DEF_ID_INJECTION);

            if (resource.getAcceptExtraFields() == null)
                resource.setAcceptExtraFields(DEF_EXTRA_FIELDS);

            for (Field field : resource.getFields())
                addFieldDefaultValues(field);

            Set<String> newIndex = new HashSet<>();
            for (String idx : resource.getIndex())
                newIndex.add(idx.startsWith("-") || idx.startsWith("+") ? idx : "+" + idx); // Ascending order by default

            resource.setIndex(newIndex);

            if (resource.getAcl() == null)
                resource.setAcl(DEF_ACL);

            for (Relation relation : resource.getRelations()) {
                if (relation.getMin() == null)
                    relation.setMin((long) DEF_MIN_INSTANCES);

                if (relation.getMax() == null)
                    relation.setMax((long) DEF_MAX_INSTANCES);

                for (Field field : relation.getFields())
                    addFieldDefaultValues(field);
            }
        }
        return this;
    }
}
