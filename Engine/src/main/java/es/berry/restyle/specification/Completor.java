package es.berry.restyle.specification;

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

    final private Spec spec;

    public Completor(Spec spec) {
        this.spec = spec;
    }

    public Spec getSpec() {
        return this.spec;
    }

    private Set<Object> getUserRoleNames() {
        Set<Object> names = new HashSet<>();

        for (Role role : spec.getRoles())
            names.add(role.getName());

        return names;
    }

    private String getGuestRole() {
        for (Role role : spec.getRoles())
            if (role.getIsGuest() != null && role.getIsGuest())
                return role.getName();

        return null;
    }

    private void addUserFields(Resource res) {
        final List<String> userReservedFieldNames = Arrays.asList("username", "role", "password", "isAdmin");

        Field name = new Field();
        Field role = new Field();
        Field pass = new Field();
        Field isAdmin = new Field();

        name.setName("username");
        name.setDescription("Name that uniquely identifies the user in the system");
        name.setType(Types.STRING);
        name.setRequired(true);
        name.setPattern("^[a-zA-z0-9_]{4,32}$");
        name.setMin(4);
        name.setMax(32);
        name.setDefault("");
        name.setUnique(true);

        role.setName("role");
        role.setDescription("Role of the user");
        role.setType(Types.STRING);
        role.setRequired(true);
        role.setMin(2);
        role.setMax(64);
        role.setEnum(getUserRoleNames());
        if (getGuestRole() != null)
            role.setDefault(getGuestRole());

        pass.setName("password");
        pass.setDescription("Password the user must use for authentication");
        pass.setType(Types.STRING);
        pass.setRequired(true);
        pass.setMin(8);
        pass.setMax(512);
        pass.setFilterable(false);
        pass.setSortable(false);
        pass.setWriteOnly(true);
        pass.setEncrypted(true);

        isAdmin.setName("isAdmin");
        isAdmin.setDescription("Determines whether the user is a super administrator or not");
        isAdmin.setType(Types.BOOL);
        isAdmin.setRequired(true);
        isAdmin.setDefault(0);

        Set<Field> fields = res.getFields();

        fields.removeIf(f -> userReservedFieldNames.contains(f.getName()));

        fields.addAll(Arrays.asList(name, role, pass, isAdmin));
        res.setFields(fields);
    }

    private static void addFieldDefaultValues(Field field) {
        if (field.getMin() == null && Types.MIN_MAX_INT.contains(field.getType()))
            field.setMin(DEF_MIN);

        if (field.getMax() != null && field.getMax().equals(SPECIAL_MAX))
            field.setMax(null);

        if (field.getAutoIncrement() == null)
            field.setAutoIncrement(DEF_AUTO_INCREMENT);
        else if (field.getAutoIncrement())
            field.setType(Types.INT);

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

            if (role.getRateLimit() != null && role.getRateLimit().getNumOfRequests() == null)
                role.getRateLimit().setNumOfRequests((long) DEF_RATE_LIMIT_REQ_NUM);
        }

        for (Resource resource : spec.getResources()) {
            if (resource.getIsUser() == null)
                resource.setIsUser(DEF_IS_USER);
            else if (resource.getIsUser())
                addUserFields(resource);

            if (Strings.isEmpty(resource.getPlural()))
                resource.setPlural(English.plural(resource.getName()));

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

            // TODO: acl

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
