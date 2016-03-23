package es.berry.restyle.core;

import es.berry.restyle.specification.*;
import es.berry.restyle.utils.Strings;
import org.atteo.evo.inflector.English;

// IDEA: apply decorator pattern: SpecWithDefaults, SpecWithResolvedTypes
final public class SpecCompletor {
    // Global
    private final static String DEF_ENCODING = "UTF-8";
    private final static String DEF_TIME_ZONE = "UTC";
    private final static String DEF_DATE_FMT = "ISO-8601";
    private final static boolean DEF_IS_ADMIN = false;
    private final static boolean DEF_IS_GUEST = false;
    private final static int DEF_RATE_LIMIT_REQ_NUM = 0;
    // Resources
    private final static boolean DEF_ABSTRACT = false;
    private final static boolean DEF_PAGINABLE = true;
    private static final boolean DEF_ID_INJECTION = false;
    private final static int DEF_MIN_INSTANCES = 0;
    private final static int DEF_MAX_INSTANCES = 0;
    // Fields
    private final static int DEF_MIN = 0;
    private final static int DEF_MAX = 0;
    private static final boolean DEF_REQUIRED = false;
    private static final boolean DEF_UNIQUE = false;
    private final static boolean DEF_FILTERABLE = true;
    private final static boolean DEF_SORTABLE = true;
    private final static boolean DEF_WRITE_ONLY = false;
    private final static boolean DEF_READ_ONLY = false;
    private final static boolean DEF_ENCRYPTED = false;

    final private Spec spec;

    public SpecCompletor(Spec spec) {
        this.spec = spec;
    }

    public Spec getSpec() {
        return this.spec;
    }

    private static void addFieldDefaultValues(Field field) {
        if (field.getMin() == null)
            field.setMin((long) DEF_MIN);

        if (field.getMax() == null)
            field.setMax((long) DEF_MAX);

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

    public SpecCompletor addDefaultValues() {
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
            if (Strings.isEmpty(resource.getPlural()) && !Strings.isEmpty(resource.getDisplayName()))
                resource.setPlural(English.plural(resource.getDisplayName()));

            if (resource.getAbstract() == null)
                resource.setAbstract(DEF_ABSTRACT);

            if (resource.getPaginable() == null)
                resource.setPaginable(DEF_PAGINABLE);

            if (resource.getIdInjection() == null)
                resource.setIdInjection(DEF_ID_INJECTION);

            for (Field field : resource.getFields())
                addFieldDefaultValues(field);

            for (String idx : resource.getIndex())
                if (!idx.startsWith("-") || !idx.startsWith("+"))
                    // FIXME: enough for modifying the real idx in the resource?
                    // TODO: Java test for the sentence right above
                    idx = "+" + idx; // Ascending order by default

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
