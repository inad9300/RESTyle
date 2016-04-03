package es.berry.restyle.generators;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.berry.restyle.core.Generator;
import es.berry.restyle.core.TemplateGen;
import es.berry.restyle.exceptions.PluginException;
import es.berry.restyle.generators.interfaces.SqlCarrier;
import es.berry.restyle.logging.Log;
import es.berry.restyle.logging.Logger;
import es.berry.restyle.specification.Types;
import es.berry.restyle.specification.generated.Field;
import es.berry.restyle.specification.generated.Relation;
import es.berry.restyle.specification.generated.Resource;
import es.berry.restyle.specification.generated.Spec;
import es.berry.restyle.utils.Strings;

import java.io.File;
import java.io.IOException;
import java.util.*;

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

    public MysqlCreationScript(Spec spec, File out) {
        super(spec, out);
        this.setTemplateGen(new TemplateGen(MysqlCreationScript.class, "sql"));
    }

    @Override
    public void generate() {
        String result = getInitialConfig() + "\n\n";

        final int numOfResources = this.getSpec().getResources().size();

        // Generate main tables first
        for (Resource res : this.getSpec().getResources())
            result += doResourcePart(res) + "\n\n";

        int count = 0;

        // Create some extra tables and alter the previous ones
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

    private String getInitialConfig() {
        final String charset = MysqlHelper.adaptStandardName(this.getSpec().getEncoding());

        ObjectNode node = new ObjectMapper().createObjectNode();
        node.put("charset", charset);
        node.put("collation", MysqlHelper.getCollation(charset));
        node.put("dbName", this.getSpec().getDatabase().getName());
        node.put("dbHost", this.getSpec().getDatabase().getHost());
        node.put("dbAdminName", this.getSpec().getDatabase().getAdmin().getName());
        node.put("dbAdminPass", this.getSpec().getDatabase().getAdmin().getPassword());

        return this.getTemplateGen().compile("initial_config", node);
    }

    // TODO: support inheritance, considering "abstract", "inheritanceStrategy", "base" and "acl"
    private String doResourcePart(Resource res) {
        if (res.getAbstract())
            return "";

        final String tableName = res.getName();

        if (tableName.length() > MAX_NAME_LEN)
            throw new PluginException("The resource " + tableName + " has a name bigger than "
                    + MAX_NAME_LEN + " characters, so a table cannot be created for it.");

        List<String> s = new ArrayList<>();
        s.add("CREATE TABLE " + Strings.surround(tableName, REVERSE_QUOTE) + " (");

        if (res.getIdInjection())
            s.add("\t`id` " + KEY_MODIFIERS + " AUTO_INCREMENT PRIMARY KEY" + ",");

        final Set<Field> fields = res.getFields();
        assert fields != null && fields.size() > 0;

        final String fieldsPart = doFieldsPart(fields);
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

    private String doFieldsPart(Collection<Field> fields) {
        List<String> fieldsPart = new ArrayList<>();
        for (Field field : fields)
            fieldsPart.add("\t"
                    + Strings.surround(field.getName(), REVERSE_QUOTE)
                    + " " + MysqlHelper.getType(field)
                    + " " + MysqlHelper.getTypeModifier(field));

        return Strings.join(fieldsPart, ",\n");
    }

    private String doPrimaryKeyPart(Resource res) {
        // Not defined by spec.
        return null;
    }

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

    private String doUniquePart(Resource res) {
        return null;
    }

    public String getForeignKey(String name) {
        return "id_" + name;
    }

    public String getManyToManyTableName(String resA, String resB) {
        if (resA.compareTo(resB) < 0)
            return resA + "_" + resB;
        else
            return resB + "_" + resA;
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

        // TODO: avoid if the maximum is the maximum of the type, or if the minimum is greater or equal to zero for certain types
        switch (field.getType()) {
            case Types.STRING:
                if (field.getMax() != null)
                    checks.add("char_length(" + quotedName + ") <= " + field.getMax());

                if (field.getMin() != null && ((Number) field.getMin()).intValue() != 0)
                    checks.add("char_length(" + quotedName + ") >= " + field.getMin());
                break;
            case Types.FILE:
                if (field.getMax() != null && ((Number) field.getMin()).intValue() != 0)
                    checks.add("length(" + quotedName + ") <= " + field.getMax());

                if (field.getMin() != null)
                    checks.add("length(" + quotedName + ") >= " + field.getMin());
                break;
            case Types.INT:
            case Types.FLOAT:
            case Types.DECIMAL:
                if (field.getMax() != null)
                    checks.add(quotedName + " <= " + field.getMax());

                if (field.getMin() != null)
                    checks.add(quotedName + " >= " + field.getMin());
                break;
            case Types.DATE:
            case Types.TIME:
            case Types.DATETIME:
                String fmt = null;
                switch (field.getType()) {
                    case Types.DATE:
                        fmt = MysqlHelper.ISO_8601_UTC_DATE;
                        break;
                    case Types.TIME:
                        fmt = MysqlHelper.ISO_8601_UTC_DATE;
                        break;
                    case Types.DATETIME:
                        fmt = MysqlHelper.ISO_8601_UTC_DATE;
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
        +----------+        +-------+
        | users    |--------| prefs |
        +----------+        +-------+
        | id       |        | id    |
        | id_prefs |        +-------+
        +----------+
     */
    private String doOneToOneRelationshipsPart(Resource res) {
        List<String> stmts = new ArrayList<>();

        for (Relation rel : res.getRelations())
            if (rel.getType().toString().equals(HAS_ONE)) {
                final String newCol = Strings.surround(getForeignKey(rel.getWith()), REVERSE_QUOTE);
                stmts.add("ALTER TABLE " + Strings.surround(res.getName(), REVERSE_QUOTE)
                        + "\nADD COLUMN " + newCol + " " + KEY_MODIFIERS + ","
                        + "\nADD FOREIGN KEY (" + newCol + ") REFERENCES " + Strings.surround(rel.getWith(), REVERSE_QUOTE) + "(`id`)"
                        + addReferenceOptions(rel) + ";");
            }

        return Strings.join(stmts, "\n\n");
    }

    private String addReferenceOptions(Relation rel) {
        return (rel.getOnDelete() == null ? "" : " ON DELETE " + rel.getOnDelete().toUpperCase()) +
                (rel.getOnUpdate() == null ? "" : " ON UPDATE " + rel.getOnUpdate().toUpperCase());
    }

    private String doOneToManyRelationshipsPart(Resource res) {
        List<String> stmts = new ArrayList<>();

        for (Relation rel : res.getRelations())
            if (rel.getType().toString().equals(HAS_MANY) && !hasHasManyRelationship(rel, res)) {
                final String newCol = Strings.surround(getForeignKey(res.getName()), REVERSE_QUOTE);
                stmts.add("ALTER TABLE " + Strings.surround(rel.getWith(), REVERSE_QUOTE)
                        + "\nADD COLUMN " + newCol + " " + KEY_MODIFIERS + ","
                        + "\nADD FOREIGN KEY (" + newCol + ") REFERENCES " + Strings.surround(res.getName(), REVERSE_QUOTE) + "(`id`)"
                        + addReferenceOptions(rel) + ";");
            }

        return Strings.join(stmts, "\n\n");
    }

    // Checks if there is a relationship between two resources, one given as a Relation object.
    // To make it easier to understand, variables are named considering that we are looking
    // for a relationship from books back to users.
    private boolean hasHasManyRelationship(Relation booksAsRelation, Resource usersAsResource) {
        Resource booksAsResource = findResourceByName(booksAsRelation.getWith());

        if (booksAsResource == null) return false;

        for (Relation booksRel : booksAsResource.getRelations())
            if (booksRel.getWith().equals(usersAsResource.getName()))
                return true;

        return false;
    }

    // TODO: support "reflective" relationships (users -- users (as friends))
    private String doManyToManyRelationshipsPart(Resource resA) {
        if (resourcesWithManyToManyRelationshipsCreated.contains(resA.getName()))
            return "";

        List<String> stmts = new ArrayList<>();

        for (Relation relA : resA.getRelations())
            if (relA.getType().toString().equals(HAS_MANY) && hasHasManyRelationship(relA, resA)) {
                Resource resB = findResourceByName(relA.getWith());
                assert resB != null;

                Relation relB = findRelationByName(resB, resA.getName());
                assert relB != null;

                resourcesWithManyToManyRelationshipsCreated.add(resA.getName());
                resourcesWithManyToManyRelationshipsCreated.add(resB.getName());

                Set<Field> allFields = new HashSet<>();
                allFields.addAll(relA.getFields());
                allFields.addAll(relB.getFields());

                Set<String> allIndexes = new HashSet<>();
                allIndexes.addAll(relA.getIndex());
                allIndexes.addAll(relB.getIndex());

                Set<String> allChecks = new HashSet<>();
                if (!Strings.isEmpty(relA.getCheck()))
                    allChecks.add(Strings.surround(relA.getCheck(), "(", ")"));
                if (!Strings.isEmpty(relB.getCheck()))
                    allChecks.add(Strings.surround(relB.getCheck(), "(", ")"));

                final String newTableQ = Strings.surround(
                        getManyToManyTableName(resA.getName(), resB.getName()), REVERSE_QUOTE);

                final String fieldsPart = doFieldsPart(allFields);
                final String indexPart = doIndexPart(allIndexes);
                // final String uniquePart = doUniquePart(allFields);
                final String checkPart = doCheckPart(allFields, Strings.join(allChecks, " AND ", true));

                stmts.add("CREATE TABLE " + newTableQ + " (\n"
                        + Strings.join(Arrays.asList(fieldsPart, indexPart, checkPart), ",\n", true)
                        + "\n);");

                final String newColA = Strings.surround(getForeignKey(resA.getName()), REVERSE_QUOTE);
                final String newColB = Strings.surround(getForeignKey(resB.getName()), REVERSE_QUOTE);

                stmts.add("ALTER TABLE " + Strings.surround(relA.getWith(), REVERSE_QUOTE)
                        + "\nADD COLUMN " + newColA + " " + KEY_MODIFIERS + ","
                        + "\nADD COLUMN " + newColB + " " + KEY_MODIFIERS + ","
                        + "\nADD FOREIGN KEY (" + newColA + ") REFERENCES " + Strings.surround(resA.getName(), REVERSE_QUOTE) + "(`id`)"
                        + addReferenceOptions(relA) + ","
                        + "\nADD FOREIGN KEY (" + newColB + ") REFERENCES " + Strings.surround(resB.getName(), REVERSE_QUOTE) + "(`id`)"
                        + addReferenceOptions(relB)
                        + ";");
            }

        return Strings.join(stmts, "\n\n");
    }

    private Resource findResourceByName(String name) {
        for (Resource res : this.getSpec().getResources())
            if (res.getName().equals(name))
                return res;

        return null;
    }

    private Relation findRelationByName(Resource res, String relName) {
        for (Relation rel : res.getRelations())
            if (rel.getWith().equals(relName))
                return rel;

        return null;
    }
}