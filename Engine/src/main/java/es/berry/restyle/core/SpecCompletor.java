package es.berry.restyle.core;

import es.berry.restyle.specification.*;
import es.berry.restyle.utils.Strings;
import org.atteo.evo.inflector.English;

// IDEA: apply decorator pattern: SpecWithDefaults, SpecWithResolvedTypes
final public class SpecCompletor {

    final private Spec spec;

    public SpecCompletor(Spec spec) {
        this.spec = spec;
    }

    public Spec getSpec() {
        return this.spec;
    }

    private static void addFieldDefaultValues(Field field) {
        if (field.getMin() == null)
            field.setMin(0);

        if (field.getMax() == null)
            field.setMax(0);

        if (field.getFilterable() == null)
            field.setFilterable(true);

        if (field.getSortable() == null)
            field.setSortable(true);

        if (field.getWriteOnly() == null)
            field.setWriteOnly(false);

        if (field.getReadOnly() == null)
            field.setReadOnly(false);

        if (field.getEncrypted() == null)
            field.setEncrypted(false);
    }

    public SpecCompletor addDefaultValues() {
        if (Strings.isEmpty(spec.getEncoding()))
            spec.setEncoding("UTF-8");

        if (Strings.isEmpty(spec.getTimeZone()))
            spec.setTimeZone("UTC");

        if (Strings.isEmpty(spec.getDateFormat()))
            spec.setDateFormat("ISO-8601");

        for (Role role : spec.getRoles()) {
            if (role.getIsAdmin() == null)
                role.setIsAdmin(false);

            if (role.getIsGuest() == null)
                role.setIsGuest(false);

            if (role.getRateLimit() != null && role.getRateLimit().getNumOfRequests() == null)
                role.getRateLimit().setNumOfRequests(0);
        }

        for (Resource resource : spec.getResources()) {
            if (Strings.isEmpty(resource.getPlural()) && !Strings.isEmpty(resource.getDisplayName()))
                resource.setPlural(English.plural(resource.getDisplayName()));

            if (resource.getAbstract() == null)
                resource.setAbstract(false);

            if (resource.getPaginable() == null)
                resource.setPaginable(true);

            for (Field field : resource.getFields()) {
                addFieldDefaultValues(field);
            }

            // TODO: index, acl

            for (Relation relation : resource.getRelations()) {
                if (relation.getMin() == null)
                    relation.setMin(0);

                if (relation.getMax() == null)
                    relation.setMax(0);

                for (Field field : relation.getFields()) {
                    addFieldDefaultValues(field);
                }
            }
        }
        return this;
    }
}
