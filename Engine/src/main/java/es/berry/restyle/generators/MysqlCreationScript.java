package es.berry.restyle.generators;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.berry.restyle.core.Generator;
import es.berry.restyle.core.TemplateGen;
import es.berry.restyle.exceptions.PluginException;
import es.berry.restyle.logging.Log;
import es.berry.restyle.logging.Logger;
import es.berry.restyle.specification.generated.Field;
import es.berry.restyle.specification.generated.Relation;
import es.berry.restyle.specification.generated.Resource;
import es.berry.restyle.specification.generated.Spec;
import es.berry.restyle.utils.Strings;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MysqlCreationScript extends Generator {
    private static final String SINGLE_QUOTE = "'";
    private static final String REVERSE_QUOTE = "`";
    private static final int MAX_NAME_LEN = 64; // Applies to (at least) databases, tables, columns, indexes, constraints and views (see http://dev.mysql.com/doc/refman/5.7/en/identifiers.html)

    private static final String HAS_ONE = "hasOne";
    private static final String HAS_MANY = "hasMany";
    private static final String KEY_MODIFIERS = "INT UNSIGNED NOT NULL"; // FIXME: NOT NULL??

    private static final Logger log = Log.getChain();

    public MysqlCreationScript(Spec spec, File out) {
        super(spec, out);
        this.tmpl = new TemplateGen(MysqlCreationScript.class, "sql");
    }

    @Override
    public void generate() { // TODO: better vertical spacing
        String result = getInitialConfig();

        for (Resource res : spec.getResources())
            result += doResourcePart(res);

        try {
            Strings.toFile(result, "generate_database.sql");
        } catch (IOException e) {
            log.error("Error creating file in plugin " + this.getClass().getSimpleName(), e);
        }
    }

    private String getInitialConfig() {
        final String charset = MysqlHelper.adaptStandardName(spec.getEncoding());

        ObjectNode node = new ObjectMapper().createObjectNode();
        node.put("charset", charset);
        node.put("dbName", spec.getDatabase().getName());

        return tmpl.compile("initial_config", node);
    }

    private String createUsersAndGrantPrivileges() {
        // GRANT ALL PRIVILEGES ON `my_db`.* TO `username`@localhost IDENTIFIED BY 'password';
        return null;
    }

    private String doResourcePart(Resource res) {
        List<String> parts = new ArrayList<String>();

        parts.add(createTable(res));
        parts.add(doOneToOneRelationshipsPart(res)); // FIXME: Some "ALTER TABLE" statements may be placed before the table they reference is created
        parts.add(doOneToManyRelationshipsPart(res));
        parts.add(doManyToManyRelationshipsPart(res));

        return Strings.join(parts, "\n\n");
    }

    //    TODO: support inheritance
//    res.getAbstract();
//    res.getInheritanceStrategy();
//    res.getBase();
//    res.getAcl();
    private String createTable(Resource res) {
        if (res.getAbstract())
            return "";

        final String tableName = res.getName();

        if (tableName.length() > MAX_NAME_LEN)
            throw new PluginException("The resource " + tableName + " has a name bigger than "
                    + MAX_NAME_LEN + " characters, so a table cannot be created for it.");

        List<String> s = new ArrayList<String>();
        s.add("CREATE TABLE " + Strings.surround(tableName, REVERSE_QUOTE) + " (");

        if (res.getIdInjection())
            s.add("\t`id` " + KEY_MODIFIERS + " AUTO_INCREMENT PRIMARY KEY" + ",");

        Set<Field> fields = res.getFields();
        assert fields != null && fields.size() > 0;

        s.add(getFieldsPart(fields));

        // s.add("\t" + doPrimaryKeyPart(res));
        s.add("\t" + doIndexPart(res));
        // s.add("\t" + doUniquePart(res));
        s.add("\t" + doCheckPart(res));

        s.add(");");

        s.add(doTableOptionsPart(res));

        return Strings.join(s, "\n");
    }

    private String getFieldsPart(Collection<Field> fields) {
        List<String> fieldsPart = new ArrayList<String>();
        for (Field field : fields)
            fieldsPart.add("\t"
                    + Strings.surround(field.getName(), REVERSE_QUOTE)
                    + MysqlHelper.getType(field)
                    + MysqlHelper.getTypeModifier(field));

        return Strings.join(fieldsPart, ",\n");
    }

    private String doPrimaryKeyPart(Resource res) {
        return null;
    }

    private String doIndexPart(Resource res) {
        if (res.getIndex() == null)
            return "";

        String finalIdx = "INDEX (";
        for (String idx : res.getIndex()) {
            assert idx.startsWith("+") || idx.startsWith("-");

            if (idx.startsWith("+"))
                finalIdx += idx.substring(1) + " ASC";
            else
                finalIdx += idx.substring(1) + " DESC";
        }
        return finalIdx;
    }

    private String doUniquePart(Resource res) {
        return null;
    }

    private String buildForeignKey(String name) {
        return "id_" + name;
    }

    private String buildManyToManyTableName(String resA, String resB) {
        if (resA.compareTo(resB) < 0)
            return resA + "_" + resB;
        else
            return resB + "_" + resA;
    }

    private String doCheckPart(Resource res) {
        List<String> checks = new ArrayList<String>();

        for (Field field : res.getFields())
            checks.addAll(getFieldChecks(field));

        final String generalCheck = "(" + res.getCheck() + ")";
        checks.add(generalCheck);

        return "CHECK (" + Strings.join(checks, " AND ") + ")";
    }

    private List<String> getFieldChecks(Field field) {
        List<String> checks = new ArrayList<String>();

        if (field.getMax() != null)
            checks.add(field.getName() + " <= " + field.getMax());

        if (field.getMin() != null)
            checks.add(field.getName() + " >= " + field.getMin());

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
        List<String> alterStmts = new ArrayList<String>();

        for (Relation rel : res.getRelations())
            if (rel.getType().equals(HAS_ONE)) {
                final String newCol = Strings.surround(buildForeignKey(rel.getWith()), REVERSE_QUOTE);
                alterStmts.add(
                        "ALTER TABLE " + Strings.surround(res.getName(), REVERSE_QUOTE)
                                + "\nADD COLUMN " + newCol + KEY_MODIFIERS + ","
                                + "\nADD FOREIGN KEY (" + newCol + ") REFERENCES "
                                + Strings.surround(rel.getWith(), REVERSE_QUOTE) + "(`id`)"
                                + addReferenceOptions(rel)
                );
            }

        return Strings.join(alterStmts, "\n\n");
    }

    private String addReferenceOptions(Relation rel) {
        return (rel.getOnDelete() == null ? "" : " " + rel.getOnDelete()) +
                (rel.getOnUpdate() == null ? "" : " " + rel.getOnUpdate());
    }

    private String doOneToManyRelationshipsPart(Resource res) {
        List<String> alterStmts = new ArrayList<String>();

        for (Relation rel : res.getRelations())
            if (rel.getType().equals(HAS_MANY) && !hasHasManyRelationship(rel, res)) {
                final String newCol = Strings.surround(buildForeignKey(res.getName()), REVERSE_QUOTE);
                alterStmts.add(
                        "ALTER TABLE " + Strings.surround(rel.getWith(), REVERSE_QUOTE)
                                + "\nADD COLUMN " + newCol + KEY_MODIFIERS + ","
                                + "\nADD FOREIGN KEY (" + newCol + ") REFERENCES "
                                + Strings.surround(res.getName(), REVERSE_QUOTE) + "(`id`)"
                                + addReferenceOptions(rel)
                );
            }

        return Strings.join(alterStmts, "\n\n");
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

    private List<String> resourcesWithManyToManyRelationshipsCreated = new ArrayList<String>();

    // TODO: support "reflective" relationships (users -- users (as friends))
    private String doManyToManyRelationshipsPart(Resource resA) {
        if (resourcesWithManyToManyRelationshipsCreated.contains(resA.getName()))
            return "";
        else
            resourcesWithManyToManyRelationshipsCreated.add(resA.getName());

        List<String> stmts = new ArrayList<String>();

        for (Relation relA : resA.getRelations())
            if (relA.getType().equals(HAS_MANY) && hasHasManyRelationship(relA, resA)) {
                Resource resB = findResourceByName(relA.getWith());
                assert resB != null;
                resourcesWithManyToManyRelationshipsCreated.add(resB.getName());
                Relation relB = findRelationByName(resB);

                Set<Field> allFields = new HashSet<Field>();
                allFields.addAll(resA.getFields());
                allFields.addAll(resB.getFields());

                final String newTableQ = Strings.surround(buildManyToManyTableName(resA.getName(), resB.getName()), REVERSE_QUOTE);

                stmts.add("CREATE TABLE " + newTableQ + " ("
                        + "\n" + getFieldsPart(allFields) + ","
                        + "\n");

                final String newCol = Strings.surround(buildForeignKey(resA.getName()), REVERSE_QUOTE);
                stmts.add(
                        "ALTER TABLE " + Strings.surround(relA.getWith(), REVERSE_QUOTE)
                                + "\nADD COLUMN " + newCol + KEY_MODIFIERS + ","
                                + "\nADD FOREIGN KEY (" + newCol + ") REFERENCES "
                                + Strings.surround(resA.getName(), REVERSE_QUOTE) + "(`id`)"
                                + addReferenceOptions(relA)
                );
            }

        return Strings.join(stmts, "\n\n");
    }

    private Resource findResourceByName(String name) {
        for (Resource res : spec.getResources())
            if (res.getName().equals(name))
                return res;

        return null;
    }

    private Relation findRelationByName(Resource res) {
        for (Relation rel : res.getRelations())
            if (rel.getWith().equals(res.getName()))
                return rel;

        return null;
    }
}