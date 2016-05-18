package es.berry.restyle.generators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.berry.restyle.core.Generator;
import es.berry.restyle.core.TemplateGen;
import es.berry.restyle.exceptions.PluginException;
import es.berry.restyle.generators.interfaces.SqlCarrier;
import es.berry.restyle.logging.Log;
import es.berry.restyle.logging.Logger;
import es.berry.restyle.specification.SpecHelper;
import es.berry.restyle.specification.SpecObjectMapper;
import es.berry.restyle.specification.generated.Field;
import es.berry.restyle.specification.generated.Relation;
import es.berry.restyle.specification.generated.Resource;
import es.berry.restyle.specification.generated.Spec;
import es.berry.restyle.utils.Strings;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Plugin to generate a MySQL DDL file for the creation of a database.
 */
public class MysqlCreationScript extends Generator implements SqlCarrier {
    private static final String SINGLE_QUOTE = "'";
    private static final String REVERSE_QUOTE = "`";

    // Applies to (at least) databases, tables, columns, indexes, constraints and views (see
    // http://dev.mysql.com/doc/refman/5.7/en/identifiers.html)
    private static final int MAX_NAME_LEN = 64;

    private static final String HAS_ONE = "hasOne";
    private static final String HAS_MANY = "hasMany";
    private static final String KEY_MODIFIERS = "INT UNSIGNED NOT NULL";

    private static final Logger log = Log.getChain();

    // Array to remember which M to N relationships were already treated
    private Set<String> resourcesWithManyToManyRelationshipsCreated = new HashSet<>();

    public MysqlCreationScript(Spec spec, JsonNode specNode, File out) {
        super(spec, specNode, out);
        this.setTemplateGen(new TemplateGen(MysqlCreationScript.class, "sql"));
    }

    @Override
    public void generate() {
        log.info("· Getting initial configuration...");
        String result = getInitialConfig() + "\n\n";

        log.info("· Creating resources tables...");
        for (Resource res : this.getSpec().getResources())
            result += doResourcePart(res) + "\n\n";

        final int numOfResources = this.getSpec().getResources().size();
        int count = 0;

        log.info("· Creating relationships-related tables and altering the previous ones...");
        for (Resource res : this.getSpec().getResources()) {
            List<String> parts = new ArrayList<>();

            parts.add(doOneToOneRelationshipsPart(res));
            parts.add(doOneToManyRelationshipsPart(res));
            parts.add(doManyToManyRelationshipsPart(res));

            final boolean isLastResource = ++count == numOfResources - 1;

            final String partialResult = Strings.join(parts, "\n\n", true);
            result += partialResult.isEmpty() || isLastResource ? partialResult : partialResult + "\n\n";
        }

        try {
            Strings.toFile(result, this.getOut().getAbsolutePath() + "/generate_database.sql");
        } catch (IOException e) {
            log.error("Error creating file in plugin " + this.getClass().getSimpleName(), e);
        }
    }

    /**
     * Build the initial part of the script.
     */
    private String getInitialConfig() {
        final String charset = MysqlHelper.adaptStandardName(this.getSpec().getEncoding());

        ObjectNode node = SpecObjectMapper.getInstance().createObjectNode();
        node.put("charset", charset);

        // NOTE: to display the actual list of available time zones: "select * from mysql.time_zone_name;". More
        // information in https://dev.mysql.com/doc/refman/5.7/en/time-zone-support.html
        node.put("timezone", this.getSpec().getTimeZone().equals("UTC") ? "+00:00" : this.getSpec().getTimeZone());

        node.put("collation", MysqlHelper.getCollation(charset));
        node.put("dbName", this.getSpec().getDatabase().getName());
        node.put("dbHost", this.getSpec().getDatabase().getHost());
        node.put("dbAdminName", this.getSpec().getDatabase().getAdmin().getName());
        node.put("dbAdminPass", this.getSpec().getDatabase().getAdmin().getPassword());

        // Custom attributes -- must be accessed directly via the JsonNode since the Spec class is automatically
        // generated from the main JSON Schema, but custom attributes are defined in the plugin-specific JSON Schema.
        final boolean dropFirst = this.getSpecNode().get("database").get("x-dropFirst").asBoolean(true);
        node.put("dropFirst", dropFirst);

        return this.getTemplateGen().compile("initial_config", node);
    }

    @Override
    public String getTableName(Resource res) {
        return res.getPlural();
    }

    /**
     * Take one resource and build everything the specification needs, in terms of tables.
     */
    private String doResourcePart(Resource res) {
        if (res.getAbstract())
            return "";

        final String tableName = getTableName(res);

        if (tableName.length() > MAX_NAME_LEN)
            throw new PluginException("The resource " + tableName + " has a name bigger than "
                    + MAX_NAME_LEN + " characters, so a table cannot be created for it.");

        List<String> s = new ArrayList<>();
        s.add("CREATE TABLE " + Strings.surround(tableName, REVERSE_QUOTE) + " (");

        if (res.getIdInjection())
            s.add("\t" + Strings.surround(getPrimaryKey(res), REVERSE_QUOTE) + " " + KEY_MODIFIERS + " AUTO_INCREMENT PRIMARY KEY" + ",");

        final Set<Field> fields = res.getFields();
        assert fields != null && fields.size() > 0;

        final JsonNode rawFields = SpecHelper.findResourceByName(this.getSpecNode(), res.getName()).get("fields");

        final String fieldsPart = doFieldsPart(fields, (ArrayNode) rawFields);
        // final String pkPart = doPrimaryKeyPart(res);
        final String indexPart = doIndexPart(res.getIndex());
        // final String uniquePart = doUniquePart(res);
        final String checkPart = doCheckPart(res.getFields(), res.getCheck());

        s.add(
                Strings.join(Arrays.asList(fieldsPart, indexPart, checkPart), ",\n", true)
        );

        final String optionsPart = doTableOptionsPart(res);
        if (Strings.isEmpty(optionsPart))
            s.add(");");
        else
            s.add(") " + optionsPart + ";");

        return Strings.join(s, "\n");
    }

    /**
     * Build the necessary string to create a row in a table from the information given about a field of a resource.
     */
    private String doFieldsPart(Collection<Field> fields, ArrayNode rawFields) {
        List<String> fieldsPart = new ArrayList<>();
        int i = 0;
        for (Field field : fields) {
            final ObjectNode rawField = (ObjectNode) rawFields.get(i++);
            fieldsPart.add("\t"
                    + Strings.surround(field.getName(), REVERSE_QUOTE)
                    + " " + MysqlHelper.getType(field, rawField)
                    + " " + MysqlHelper.getTypeModifier(field, rawField));
        }

        return Strings.join(fieldsPart, ",\n");
    }

//    private String doPrimaryKeyPart(Resource res) {
//        // Not defined by spec.
//        return null;
//    }

    /**
     * Build the index part of the 'CREATE TABLE' statement.
     */
    private String doIndexPart(Collection<String> indexes) {
        if (indexes == null || indexes.size() <= 0)
            return "";

        String finalIdx = "\tINDEX (";
        for (String idx : indexes) {
            assert idx.startsWith("+") || idx.startsWith("-");

            final String quotedField = Strings.surround(idx.substring(1), REVERSE_QUOTE);

            if (idx.startsWith("+"))
                finalIdx += quotedField + " ASC";
            else
                finalIdx += quotedField + " DESC";
        }
        return finalIdx + ")";
    }

//    private String doUniquePart(Resource res) {
//        return null;
//    }

    /**
     * Return the name of the primary key that will be used for a particular resource. In this case it is a constant.
     */
    @Override
    public String getPrimaryKey(Resource res) {
        return "id";
    }

    /**
     * Return the name of the foreign key for one resource.
     */
    @Override
    public String getForeignKey(Resource res) {
        return res.getName() + "_id";
    }

    /**
     * Simple algorithm to calculate the name of a table needed by a many to many relationship.
     */
    @Override
    public String getManyToManyTableName(Resource resA, Resource resB) {
        if (resA.getPlural().compareTo(resB.getPlural()) < 0)
            return resA.getPlural() + "_" + resB.getPlural();
        else
            return resB.getPlural() + "_" + resA.getPlural();
    }

    @Override
    public String getHasOneStr() {
        return HAS_ONE;
    }

    @Override
    public String getHasManyStr() {
        return HAS_MANY;
    }

    private String doCheckPart(Collection<Field> fields, String checkStr) {
        List<String> checks = new ArrayList<>();

        for (Field field : fields)
            checks.addAll(getFieldChecks(field));

        if (!Strings.isEmpty(checkStr))
            checks.add("(" + checkStr + ")");

        if (checks.size() <= 0)
            return "";

        return "\tCHECK (" + Strings.join(checks, " AND ") + ")";
    }

    private List<String> getFieldChecks(Field field) {
        List<String> checks = new ArrayList<>();

        final String quotedName = Strings.surround(field.getName(), REVERSE_QUOTE);

        switch (field.getType()) {
            case STRING:
                if (field.getMax() != null)
                    checks.add("char_length(" + quotedName + ") <= " + field.getMax());

                if (field.getMin() != null && ((Number) field.getMin()).intValue() != 0)
                    checks.add("char_length(" + quotedName + ") >= " + field.getMin());
                break;
            case FILE:
                if (field.getMax() != null && ((Number) field.getMax()).intValue() != 0)
                    checks.add("length(" + quotedName + ") <= " + field.getMax());

                if (field.getMin() != null)
                    checks.add("length(" + quotedName + ") >= " + field.getMin());
                break;
            case INT:
            case FLOAT:
            case DECIMAL:
                if (field.getMax() != null)
                    checks.add(quotedName + " <= " + field.getMax());

                if (field.getMin() != null)
                    checks.add(quotedName + " >= " + field.getMin());
                break;
            case DATE:
            case TIME:
            case DATETIME:
                String fmt = null;
                switch (field.getType()) {
                    case DATE:
                        fmt = MysqlHelper.ISO_8601_UTC_DATE;
                        break;
                    case TIME:
                        fmt = MysqlHelper.ISO_8601_UTC_TIME;
                        break;
                    case DATETIME:
                        fmt = MysqlHelper.ISO_8601_UTC_FULL;
                        break;
                }
                assert fmt != null;

                if (field.getMin() != null)
                    checks.add(quotedName + " <= str_to_date(" + field.getMin() + ", " + fmt + ")");

                if (field.getMax() != null)
                    checks.add(quotedName + " <= str_to_date(" + field.getMax() + ", " + fmt + ")");

                break;
        }

        return checks;
    }

    private String doTableOptionsPart(Resource res) {
        if (!Strings.isEmpty(res.getDescription()))
            return "COMMENT " + Strings.surround(res.getDescription(), SINGLE_QUOTE);

        return "";
    }

    /* e.g.
        +----------+        +---------+
        | users    |--------| phone   |
        +----------+        +---------+
        | id       |        | id      |
        +----------+        | user_id |
                            +---------+
     */
    private String doOneToOneRelationshipsPart(Resource res) {
        List<String> stmts = new ArrayList<>();

        for (Relation rel : res.getRelations())
            if (HAS_ONE.equals(rel.getType().toString())) {
                final String newCol = Strings.surround(getForeignKey(res), REVERSE_QUOTE);
                stmts.add("ALTER TABLE " + Strings.surround(SpecHelper.findResourceByName(this.getSpec(), rel.getWith()).getPlural(), REVERSE_QUOTE)
                        + "\nADD COLUMN " + newCol + " " + KEY_MODIFIERS + " UNIQUE,"
                        + "\nADD FOREIGN KEY (" + newCol + ") REFERENCES " + Strings.surround(res.getPlural(), REVERSE_QUOTE)
                        + "(" + Strings.surround(getPrimaryKey(res), REVERSE_QUOTE) + ")"
                        + addReferenceOptions(rel) + ";");
            }

        return Strings.join(stmts, "\n\n");
    }

    private String addReferenceOptions(Relation rel) {
        return (rel.getOnDelete() == null ? "" : " ON DELETE " + MysqlHelper.transformReferencialActions(rel.getOnDelete())) +
                (rel.getOnUpdate() == null ? "" : " ON UPDATE " + MysqlHelper.transformReferencialActions(rel.getOnUpdate()));
    }

    private String doOneToManyRelationshipsPart(Resource res) {
        List<String> stmts = new ArrayList<>();

        for (Relation rel : res.getRelations())
            if (HAS_MANY.equals(rel.getType().toString()) && !hasHasManyRelationship(rel, res)) {
                final String newCol = Strings.surround(getForeignKey(res), REVERSE_QUOTE);
                stmts.add("ALTER TABLE " + Strings.surround(SpecHelper.findResourceByName(this.getSpec(), rel.getWith()).getPlural(), REVERSE_QUOTE)
                        + "\nADD COLUMN " + newCol + " " + KEY_MODIFIERS + ","
                        + "\nADD FOREIGN KEY (" + newCol + ") REFERENCES " + Strings.surround(res.getPlural(), REVERSE_QUOTE)
                        + "(" + Strings.surround(getPrimaryKey(res), REVERSE_QUOTE) + ")"
                        + addReferenceOptions(rel) + ";");
            }

        return Strings.join(stmts, "\n\n");
    }

    // Checks if there is a relationship between two resources, one given as a Relation object.
    // To make it easier to understand, variables are named considering that we are looking
    // for a relationship from books back to users.
    private boolean hasHasManyRelationship(Relation booksAsRelation, Resource usersAsResource) {
        Resource booksAsResource = SpecHelper.findResourceByName(this.getSpec(), booksAsRelation.getWith());

        if (booksAsResource == null) return false;

        for (Relation booksRel : booksAsResource.getRelations())
            if (HAS_MANY.equals(booksRel.getType().toString()) && booksRel.getWith().equals(usersAsResource.getName()))
                return true;

        return false;
    }

    private String doManyToManyRelationshipsPart(Resource resA) {
        if (resourcesWithManyToManyRelationshipsCreated.contains(resA.getName()))
            return "";

        List<String> stmts = new ArrayList<>();

        for (Relation relA : resA.getRelations())
            if (HAS_MANY.equals(relA.getType().toString()) && hasHasManyRelationship(relA, resA)) {
                Resource resB = SpecHelper.findResourceByName(this.getSpec(), relA.getWith());
                assert resB != null;

                Relation relB = SpecHelper.findRelationByName(resB, resA.getName());
                assert relB != null;

                resourcesWithManyToManyRelationshipsCreated.add(resA.getName());
                resourcesWithManyToManyRelationshipsCreated.add(resB.getName());

                // Putting all the fields together
                final Set<Field> allFields = new HashSet<>();
                allFields.addAll(relA.getFields());
                allFields.addAll(relB.getFields());

                // Putting all the fields together, on its raw form
                final JsonNode rawRelA = SpecHelper.findRelationByName(SpecHelper.findResourceByName(this.getSpecNode(), resA.getName()), relA.getWith());
                final JsonNode rawRelB = SpecHelper.findRelationByName(SpecHelper.findResourceByName(this.getSpecNode(), resB.getName()), relB.getWith());
                final ArrayNode rawFields = SpecObjectMapper.getInstance().createArrayNode();
                if (rawRelA != null && rawRelA.hasNonNull("fields"))
                    rawFields.addAll((ArrayNode) rawRelA.get("fields"));
                if (rawRelB != null && rawRelB.hasNonNull("fields"))
                    rawFields.addAll((ArrayNode) rawRelB.get("fields"));

                Set<String> allIndexes = new HashSet<>();
                allIndexes.addAll(relA.getIndex());
                allIndexes.addAll(relB.getIndex());

                Set<String> allChecks = new HashSet<>();
                if (!Strings.isEmpty(relA.getCheck()))
                    allChecks.add(Strings.surround(relA.getCheck(), "(", ")"));
                if (!Strings.isEmpty(relB.getCheck()))
                    allChecks.add(Strings.surround(relB.getCheck(), "(", ")"));

                final String newTableQ = Strings.surround(
                        getManyToManyTableName(resA, resB), REVERSE_QUOTE);

                final String fieldsPart = doFieldsPart(allFields, rawFields);
                final String indexPart = doIndexPart(allIndexes);
                // final String uniquePart = doUniquePart(allFields);
                final String checkPart = doCheckPart(allFields, Strings.join(allChecks, " AND ", true));

                stmts.add("CREATE TABLE " + newTableQ + " (\n"
                        + Strings.join(Arrays.asList(fieldsPart, indexPart, checkPart), ",\n", true)
                        + "\n);");

                final String newColA = Strings.surround(getForeignKey(resA), REVERSE_QUOTE);
                final String newColB = Strings.surround(getForeignKey(resB), REVERSE_QUOTE);

                stmts.add("ALTER TABLE " + Strings.surround(getManyToManyTableName(resA, resB), REVERSE_QUOTE)
                        + "\nADD COLUMN " + newColA + " " + KEY_MODIFIERS + ","
                        + "\nADD COLUMN " + newColB + " " + KEY_MODIFIERS + ","
                        + "\nADD FOREIGN KEY (" + newColA + ") REFERENCES " + Strings.surround(resA.getPlural(), REVERSE_QUOTE)
                        + "(" + Strings.surround(getPrimaryKey(resA), REVERSE_QUOTE) + ")"
                        + addReferenceOptions(relA) + ","
                        + "\nADD FOREIGN KEY (" + newColB + ") REFERENCES " + Strings.surround(resB.getPlural(), REVERSE_QUOTE)
                        + "(" + Strings.surround(getPrimaryKey(resB), REVERSE_QUOTE) + ")"
                        + addReferenceOptions(relB)
                        + ";");
            }

        return Strings.join(stmts, "\n\n");
    }
}