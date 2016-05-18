package es.berry.restyle.generators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.berry.restyle.core.Generator;
import es.berry.restyle.core.TemplateGen;
import es.berry.restyle.generators.interfaces.SqlCarrier;
import es.berry.restyle.logging.Log;
import es.berry.restyle.logging.Logger;
import es.berry.restyle.specification.SpecHelper;
import es.berry.restyle.specification.SpecObjectMapper;
import es.berry.restyle.specification.generated.*;
import es.berry.restyle.utils.Strings;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Plugin created for the framework Lumen, version 5.2. It aims to generate a complete and fully functional REST
 * service.
 * <p>
 * External documentation available in https://lumen.laravel.com/docs/5.2 and https://laravel.com/docs/5.2
 */
public class PhpLumen extends Generator {
    private static String HAS_MANY = null;
    private final Path serverOut;
    private final Path modelsOut;
    private final Path policiesOut;
    private final Path controllersOut;
    private static String HAS_ONE = null;

    // It is used to prepend to different values and thus fix Handlebars escaping bug:
    // "\{{x}}" compiles to "{{x}}"; whereas "\\{{x}}" compiles to "\{{x}}", as oppose to "\valueOfX"
    private final String BACKSLASH = "\\";

    private static final Logger log = Log.getChain();

    public PhpLumen(Spec spec, JsonNode specNode, File outputDir) throws IOException {
        super(spec, specNode, outputDir);
        this.setTemplateGen(new TemplateGen(PhpLumen.class, "php"));
        this.prevGeneratorMustImplement(SqlCarrier.class);

        final File finalDir = new File(this.getOut().getAbsolutePath() + "/lumen-5.2-server");

        // Remove previously generated output
        log.info("· Cleaning up existing artifacts...");
        FileUtils.deleteDirectory(finalDir);

        // Store references to helpful paths
        this.serverOut = finalDir.toPath();
        this.modelsOut = new File(this.serverOut + "/app/Models").toPath();
        this.policiesOut = new File(this.serverOut + "/app/Policies").toPath();
        this.controllersOut = new File(this.serverOut + "/app/Http/Controllers").toPath();
    }

    /**
     * Initialization that needs to be done after the construction phase.
     */
    private void init() {
        // Storing some constants that this plugin must agree on with the previous plugin
        HAS_ONE = (String) this.invokePrevMethod("getHasOneStr");
        HAS_MANY = (String) this.invokePrevMethod("getHasManyStr");

        assert !Strings.isEmpty(HAS_ONE);
        assert !Strings.isEmpty(HAS_MANY);
    }

    @Override
    public void generate() {
        init();

        try {
            log.info("· Generating initial file structure...");
            getInitialConfig();

            String routes = "";

            Resource userRes = null;
            for (Resource res : this.getSpec().getResources())
                if (res.getIsUser()) {
                    userRes = res;
                    break;
                }

            log.info("· Creating models, policies (authorization), controllers and routes...");
            for (Resource res : this.getSpec().getResources()) {
                Strings.toFile(doModelPart(res), this.modelsOut + File.separator + PhpLumenHelper.getClassName(res) + ".php");
                Strings.toFile(doPolicyPart(res, userRes), this.policiesOut + File.separator + PhpLumenHelper.getClassName(res) + "Policy.php");
                Strings.toFile(doControllerPart(res), this.controllersOut + File.separator + PhpLumenHelper.getClassName(res) + "Controller.php");

                if (res.getIsUser())
                    generateAuthServiceProvider(res);

                routes += doRoutesPart(res) + "\n\n";
            }
            Strings.toFile(
                    this.getTemplateGen().compile("routes", SpecObjectMapper.getInstance().createObjectNode()
                            .put("prefix", "")
                            .put("routes", routes)),
                    this.serverOut + "/app/Http/routes.php"
            );
        } catch (IOException e) {
            log.error("Error generating some file in plugin " + this.getClass().getSimpleName(), e);
        }
    }

    /**
     * Find all the relationships pointing to the current resource, as a way to define "belongsTo" relationships (the
     * other side of the one to many and one to one relationships -- but not of many to many ones)
     */
    private Set<Resource> getResourcesBelongingToRelation(Resource res) {
        final Set<Resource> result = new HashSet<>();

        for (Resource _res : this.getSpec().getResources()) {
            if (res.getName().equals(_res.getName()))
                continue;

            // if one to one or one to many... (not many to many)
            if (SpecHelper.resourceContainsRelation(_res, res.getName(), HAS_ONE) ||
                    (SpecHelper.resourceContainsRelation(_res, res.getName(), HAS_MANY) &&
                            !SpecHelper.resourceContainsRelation(res, _res.getName(), HAS_MANY)))
                result.add(_res);
        }

        return result;
    }

    /**
     * At first, we copy a prepared folder containing a slightly modified version of Lumen 5.2 (basically, it adds some
     * extra classes), and compile some "singleton" (only one occurrence of those exist) templates.
     */
    private void getInitialConfig() throws IOException {
        // Copy seed project (slightly modified version of Lumen 5.2)
        FileUtils.copyDirectory(
                new File(this.getTemplateGen().getDefaultDir() + "lumen-seed"),
                this.serverOut.toFile()
        );

        final Database db = this.getSpec().getDatabase();

        Strings.toFile(
                new TemplateGen(PhpLumen.class).compile(".env", SpecObjectMapper.getInstance().createObjectNode()
                        .put("randomKey", new BigInteger(130, new SecureRandom()).toString(32))
                        .put("dbType", db.getDbms().toLowerCase())
                        .put("dbHost", db.getHost())
                        .put("dbPort", db.getPort())
                        .put("dbName", db.getName())
                        .put("dbUser", db.getAdmin().getName())
                        .put("dbPass", db.getAdmin().getPassword())),
                this.serverOut + "/.env"
        );

        Strings.toFile(
                this.getTemplateGen().compile("app", SpecObjectMapper.getInstance().createObjectNode()
                        .put("charset", this.getSpec().getEncoding())
                        .put("timezone", this.getSpec().getTimeZone())),
                this.serverOut + "/bootstrap/app.php"
        );

        createRoleFile();
    }

    /**
     * Compile and save the Role class, created ad hoc for Lumen to work with the roles that the specification is
     * supposed to support.
     */
    private void createRoleFile() throws IOException {
        String adminName = null;
        String guestName = null;

        ObjectMapper mapper = SpecObjectMapper.getInstance();
        ObjectNode root = mapper.createObjectNode();

        ArrayNode roles = mapper.createArrayNode();

        for (Role r : this.getSpec().getRoles()) {
            if (r.getIsAdmin())
                adminName = r.getName();
            else if (r.getIsGuest())
                guestName = r.getName();

            roles.add(mapper.createObjectNode()
                    .put("name", r.getName())
                    .put("isA", r.getIsA())
                    .put("isAdmin", r.getIsAdmin())
                    .put("isGuest", r.getIsGuest()));
        }

        root.putArray("roles").addAll(roles);
        root.put("adminName", adminName);
        root.put("guestName", guestName);

        Strings.toFile(this.getTemplateGen().compile("Role", root), this.policiesOut + "/Role.php");
    }

    /**
     * Generate the file containing all the routes that the REST service will respond to, pointing to methods on
     * classes that will be created separately.
     */
    private String doRoutesPart(Resource res) {
        ObjectMapper mapper = SpecObjectMapper.getInstance();
        ObjectNode root = mapper.createObjectNode();
        root.put("resourceName", res.getPlural());
        root.put("resourceRoute", res.getPlural());
        root.put("resourceClass", PhpLumenHelper.getClassName(res));

        final Role guest = SpecHelper.findGuestRole(this.getSpec());
        assert guest != null;
        final String guestName = guest.getName();

        root.put("guestCanReadResource", SpecHelper.roleCanRead(res, guestName));
        root.put("guestCanCreateResource", SpecHelper.roleCanCreate(res, guestName));
        root.put("guestCanUpdateResource", SpecHelper.roleCanUpdate(res, guestName));
        root.put("guestCanDeleteResource", SpecHelper.roleCanDelete(res, guestName));

        ArrayNode relations = mapper.createArrayNode();
        ArrayNode resourceFiles = mapper.createArrayNode();

        for (Relation rel : res.getRelations()) {
            final Resource relRes = SpecHelper.findResourceByName(this.getSpec(), rel.getWith());
            assert relRes != null;

            final boolean isOneToOne = HAS_ONE.equals(rel.getType().toString());
            boolean isManyToMany = HAS_MANY.equals(rel.getType().toString()) &&
                    SpecHelper.resourceContainsRelation(relRes, res.getName(), HAS_MANY);

            relations.add(mapper.createObjectNode()
                    .put("isBelongTo", false)
                    .put("isOneToOne", isOneToOne)
                    .put("isManyToMany", isManyToMany)
                    .put("subresourceName", relRes.getPlural())
                    .put("subresourceRoute", relRes.getPlural())
                    .put("subresourceClass", PhpLumenHelper.getClassName(relRes))
                    .put("subresourceClassPlural", PhpLumenHelper.getClassNamePlural(relRes))
                    .put("guestCanReadSubresource", SpecHelper.roleCanRead(relRes, guestName))
                    .put("guestCanCreateSubresource", SpecHelper.roleCanCreate(relRes, guestName))
                    .put("guestCanDeleteSubresource", SpecHelper.roleCanDelete(relRes, guestName)));
        }

        for (Resource _res : getResourcesBelongingToRelation(res))
            relations.add(mapper.createObjectNode()
                    .put("isBelongTo", true)
                    .put("isOneToOne", false)
                    .put("isManyToMany", false)
                    .put("subresourceName", _res.getPlural())
                    .put("subresourceRoute", _res.getPlural())
                    .put("subresourceClass", PhpLumenHelper.getClassName(_res))
                    .put("subresourceClassPlural", PhpLumenHelper.getClassNamePlural(_res))
                    .put("guestCanReadSubresource", SpecHelper.roleCanRead(_res, guestName))
                    .put("guestCanCreateSubresource", SpecHelper.roleCanCreate(_res, guestName))
                    .put("guestCanDeleteSubresource", SpecHelper.roleCanDelete(_res, guestName)));

        for (Field f : res.getFields())
            if (f.getType().equals(Field.Type.FILE))
                resourceFiles.add(mapper.createObjectNode()
                        .put("name", f.getName())
                        .put("camelName", Strings.studly(f.getName())));

        root.putArray("relations").addAll(relations);
        root.putArray("resourceFiles").addAll(resourceFiles);

        return this.getTemplateGen().compile("routes-resource", root);
    }

    /**
     * Create models for all the resources defined in the specification, including all the available details (that make
     * sense, namely, that Lumen/Laravel supports) on them.
     */
    private String doModelPart(Resource res) throws IOException {
        ObjectMapper mapper = SpecObjectMapper.getInstance();
        ObjectNode root = mapper.createObjectNode();

        final String resPk = (String) this.invokePrevMethod("getPrimaryKey", new Class[]{Resource.class}, res);
        final String tableName = (String) this.invokePrevMethod("getTableName", new Class[]{Resource.class}, res);

        root.put("resourceClass", PhpLumenHelper.getClassName(res));
        root.put("resourceTable", tableName);
        root.put("resourceRoute", res.getPlural());
        root.put("isUser", res.getIsUser());

        ArrayNode fillableAttributes = mapper.createArrayNode();
        ArrayNode hiddenAttributes = mapper.createArrayNode();
        ArrayNode filterableAttributes = mapper.createArrayNode();
        ArrayNode sortableAttributes = mapper.createArrayNode();
        ArrayNode casts = mapper.createArrayNode();
        ArrayNode dateAttributes = mapper.createArrayNode();
        ArrayNode timeAttributes = mapper.createArrayNode();
        ArrayNode dateTimeAttributes = mapper.createArrayNode();
        ArrayNode imageAttributes = mapper.createArrayNode();
        ArrayNode validationRules = mapper.createArrayNode();

        filterableAttributes.add(resPk);
        sortableAttributes.add(resPk);
        casts.add(mapper.createObjectNode().put("prop", resPk).put("type", "integer"));

        for (Field f : res.getFields()) {
            if (f.getFilterable())
                filterableAttributes.add(f.getName());

            if (f.getSortable())
                sortableAttributes.add(f.getName());

            if (f.getType().equals(Field.Type.DATE))
                dateAttributes.add(mapper.createObjectNode()
                        .put("name", f.getName())
                        .put("camelName", Strings.studly(f.getName())));

            if (f.getType().equals(Field.Type.TIME))
                timeAttributes.add(mapper.createObjectNode()
                        .put("name", f.getName())
                        .put("camelName", Strings.studly(f.getName())));

            if (f.getType().equals(Field.Type.DATETIME))
                dateTimeAttributes.add(mapper.createObjectNode()
                        .put("name", f.getName())
                        .put("camelName", Strings.studly(f.getName())));

            if (f.getType().equals(Field.Type.FILE))
                imageAttributes.add(mapper.createObjectNode()
                        .put("name", f.getName())
                        .put("camelName", Strings.studly(f.getName())));

            String cast = PhpLumenHelper.getCastType(f);
            if (cast != null)
                casts.add(mapper.createObjectNode()
                        .put("prop", f.getName())
                        .put("type", cast));

            if (f.getWriteOnly() || f.getEncrypted())
                hiddenAttributes.add(f.getName());

            if (!f.getReadOnly()) {
                fillableAttributes.add(f.getName());

                // All fillable attributes need validation
                final String validationRule = PhpLumenHelper.generateValidationRule(f, tableName);

                if (!Strings.isEmpty(validationRule))
                    validationRules.add(mapper.createObjectNode()
                            .put("prop", f.getName())
                            .put("rule", validationRule));
            }
        }

        ArrayNode hasOneRelations = mapper.createArrayNode();
        ArrayNode hasManyRelations = mapper.createArrayNode();
        ArrayNode belongsToRelations = mapper.createArrayNode();
        ArrayNode belongsToManyRelations = mapper.createArrayNode();

        for (Relation rel : res.getRelations()) {
            final Resource relRes = SpecHelper.findResourceByName(this.getSpec(), rel.getWith());
            final String relResClass = PhpLumenHelper.getClassName(relRes);
            final String relResFk = (String) this.invokePrevMethod("getForeignKey", new Class[]{Resource.class}, res);
            final String resFk = (String) this.invokePrevMethod("getForeignKey", new Class[]{Resource.class}, relRes);

            if (HAS_ONE.equals(rel.getType().toString()))
                hasOneRelations.add(mapper.createObjectNode()
                        .put("fn", relRes.getPlural())
                        .put("class", relResClass)
                        .put("classBack", BACKSLASH + relResClass)
                        .put("fk", relResFk)
                        .put("id", resPk));

            if (HAS_MANY.equals(rel.getType().toString())) {
                final boolean isManyToMany = SpecHelper.resourceContainsRelation(relRes, res.getName(), HAS_MANY);

                if (isManyToMany) {
                    // IDEA: improve pivot models with casts, validation rules and so on. Check
                    // http://stackoverflow.com/questions/30226496/how-to-cast-eloquent-pivot-parameters

                    Relation resRel = SpecHelper.findRelationByName(relRes, res.getName());

                    Set<Field> allFields = new HashSet<>();
                    allFields.addAll(rel.getFields());
                    allFields.addAll(resRel.getFields());

                    // Add columns to pivot table from both sides of the relation
                    ArrayNode pivotCols = mapper.createArrayNode();
                    for (Field f : allFields)
                        pivotCols.add(f.getName());

                    ObjectNode node = mapper.createObjectNode();
                    node.putArray("pivotCols").addAll(pivotCols);

                    belongsToManyRelations.add(node
                            .put("fn", relRes.getPlural())
                            .put("class", relResClass)
                            .put("classBack", BACKSLASH + relResClass)
                            .put("middleTable", (String) this.invokePrevMethod("getManyToManyTableName", new Class[]{Resource.class, Resource.class}, res, relRes))
                            .put("fk", relResFk)
                            .put("id", resFk));
                } else
                    hasManyRelations.add(mapper.createObjectNode()
                            .put("fn", relRes.getPlural())
                            .put("class", relResClass)
                            .put("classBack", BACKSLASH + relResClass)
                            .put("fk", relResFk)
                            .put("id", resPk));
            }
        }

        for (Resource _res : getResourcesBelongingToRelation(res))
            belongsToRelations.add(mapper.createObjectNode()
                    .put("fn", _res.getPlural())
                    .put("class", PhpLumenHelper.getClassName(_res))
                    .put("classBack", BACKSLASH + PhpLumenHelper.getClassName(_res))
                    .put("fk", (String) this.invokePrevMethod("getForeignKey", new Class[]{Resource.class}, _res))
                    .put("id", (String) this.invokePrevMethod("getPrimaryKey", new Class[]{Resource.class}, _res)));

        root.putArray("fillableAttributes").addAll(fillableAttributes);
        root.putArray("hiddenAttributes").addAll(hiddenAttributes);
        root.putArray("filterableAttributes").addAll(filterableAttributes);
        root.putArray("sortableAttributes").addAll(sortableAttributes);
        root.putArray("dateAttributes").addAll(dateAttributes);
        root.putArray("timeAttributes").addAll(timeAttributes);
        root.putArray("dateTimeAttributes").addAll(dateTimeAttributes);
        root.putArray("imageAttributes").addAll(imageAttributes);
        root.putArray("casts").addAll(casts);
        root.putArray("validationRules").addAll(validationRules);

        root.putArray("hasOneRelations").addAll(hasOneRelations);
        root.putArray("hasManyRelations").addAll(hasManyRelations);
        root.putArray("belongsToRelations").addAll(belongsToRelations);
        root.putArray("belongsToManyRelations").addAll(belongsToManyRelations);

        return this.getTemplateGen().compile("Model", root);
    }

    /**
     * For each resource, create a class holding the information about which roles have access to it, that is, a policy
     * class.
     */
    private String doPolicyPart(Resource res, Resource userRes) {
        ObjectMapper mapper = SpecObjectMapper.getInstance();
        ObjectNode root = mapper.createObjectNode();

        final String resClass = PhpLumenHelper.getClassName(res);
        root.put("resClass", resClass);
        root.put("resClassBack", BACKSLASH + resClass);
        root.put("resVar", res.getName());

        if (userRes != null) {
            final String userClass = PhpLumenHelper.getClassName(userRes);
            root.put("userClass", userClass);
            root.put("userClassBack", BACKSLASH + userClass);
        }

        if (userRes != res)
            root.put("differentRes", true);

        ArrayNode readRoles = mapper.createArrayNode();
        ArrayNode createRoles = mapper.createArrayNode();
        ArrayNode updateRoles = mapper.createArrayNode();
        ArrayNode deleteRoles = mapper.createArrayNode();

        final Object acl = res.getAcl();

        if (acl instanceof ArrayList) { // e.g. ['admin', 'user']
            for (Object role : (ArrayList) acl)
                if (role instanceof String) {
                    String r = (String) role;
                    readRoles.add(r);
                    createRoles.add(r);
                    updateRoles.add(r);
                    deleteRoles.add(r);
                }
        } else if (acl instanceof LinkedHashMap) { // e.g. { 'read': ['user'], 'delete: ['admin'] }
            LinkedHashMap rolesMap = (LinkedHashMap) acl;
            Object readPart = rolesMap.get("read");
            Object createPart = rolesMap.get("create");
            Object updatePart = rolesMap.get("update");
            Object deletePart = rolesMap.get("delete");

            if (readPart instanceof ArrayList)
                for (Object role : (ArrayList) readPart)
                    if (role instanceof String)
                        readRoles.add((String) role);

            if (createPart instanceof ArrayList)
                for (Object role : (ArrayList) createPart)
                    if (role instanceof String)
                        createRoles.add((String) role);

            if (updatePart instanceof ArrayList)
                for (Object role : (ArrayList) updatePart)
                    if (role instanceof String)
                        updateRoles.add((String) role);

            if (deletePart instanceof ArrayList)
                for (Object role : (ArrayList) deletePart)
                    if (role instanceof String)
                        deleteRoles.add((String) role);
        }

        root.putArray("readRoles").addAll(readRoles);
        root.putArray("createRoles").addAll(createRoles);
        root.putArray("updateRoles").addAll(updateRoles);
        root.putArray("deleteRoles").addAll(deleteRoles);

        return this.getTemplateGen().compile("Policy", root);
    }

    /**
     * Generate a slightly modified version of the Lumen's AuthServiceProvider class, for it to know how to return a
     * user from the database, thus making possible the authentication.
     */
    private void generateAuthServiceProvider(Resource res) throws IOException {
        final String userClass = PhpLumenHelper.getClassName(res);

        ObjectMapper mapper = SpecObjectMapper.getInstance();
        ObjectNode root = mapper.createObjectNode();

        final ArrayNode resources = mapper.createArrayNode();

        for (Resource r : this.getSpec().getResources()) {
            final String className = PhpLumenHelper.getClassName(r);

            resources.add(mapper.createObjectNode()
                    .put("class", className)
                    .put("classBack", BACKSLASH + className)
            );
        }

        root.put("userClass", userClass);
        root.put("userClassBack", BACKSLASH + userClass);
        root.putArray("resources").addAll(resources);

        Strings.toFile(
                this.getTemplateGen().compile("AuthServiceProvider", root),
                this.serverOut + "/app/Providers/AuthServiceProvider.php"
        );
    }

    /**
     * Generate one controller per resource, containing the actual implementation of the methods that the routes are
     * expecting. They will contain the actions related with the resource they are associated with, plus those regarding
     * the related resources, when it makes sense.
     */
    private String doControllerPart(Resource res) {
        ObjectMapper mapper = SpecObjectMapper.getInstance();
        ObjectNode root = mapper.createObjectNode();

        // final String tableName = (String) this.invokePrevMethod("getTableName", new Class[]{Resource.class}, res);

        root.put("resourceNamePlural", res.getPlural());
        root.put("resourceClass", PhpLumenHelper.getClassName(res));
        root.put("resourceClassBack", BACKSLASH + PhpLumenHelper.getClassName(res));
        root.put("resourceId", (String) this.invokePrevMethod("getPrimaryKey", new Class[]{Resource.class}, res));

        ArrayNode relations = mapper.createArrayNode();
        ArrayNode resourceFiles = mapper.createArrayNode();

        for (Relation rel : res.getRelations()) {
            final Resource relRes = SpecHelper.findResourceByName(this.getSpec(), rel.getWith());
            assert relRes != null;

            final boolean isOneToOne = HAS_ONE.equals(rel.getType().toString());
            final boolean isManyToMany = HAS_MANY.equals(rel.getType().toString()) &&
                    SpecHelper.resourceContainsRelation(relRes, res.getName(), HAS_MANY);

            relations.add(mapper.createObjectNode()
                    .put("isBelongTo", false)
                    .put("isOneToOne", isOneToOne)
                    .put("isManyToMany", isManyToMany)
                    .put("subresourceNamePlural", relRes.getPlural())
                    .put("subresourceClass", PhpLumenHelper.getClassName(relRes))
                    .put("subresourceClassBack", BACKSLASH + PhpLumenHelper.getClassName(relRes))
                    .put("subresourceClassPlural", PhpLumenHelper.getClassNamePlural(relRes))
                    .put("subresourceId", (String) this.invokePrevMethod("getPrimaryKey", new Class[]{Resource.class}, relRes))
                    .put("subFn", relRes.getPlural()));
        }

        for (Resource _res : getResourcesBelongingToRelation(res))
            relations.add(mapper.createObjectNode()
                    .put("isBelongTo", true)
                    .put("isOneToOne", false)
                    .put("isManyToMany", false)
                    .put("subresourceNamePlural", _res.getPlural())
                    .put("subresourceClass", PhpLumenHelper.getClassName(_res))
                    .put("subresourceClassBack", BACKSLASH + PhpLumenHelper.getClassName(_res))
                    .put("subresourceClassPlural", PhpLumenHelper.getClassNamePlural(_res))
                    .put("subresourceId", (String) this.invokePrevMethod("getPrimaryKey", new Class[]{Resource.class}, _res))
                    .put("subFn", _res.getPlural()));

        for (Field f : res.getFields())
            if (f.getType().equals(Field.Type.FILE))
                resourceFiles.add(mapper.createObjectNode()
                        .put("name", f.getName())
                        .put("camelName", Strings.studly(f.getName())));

        root.putArray("relations").addAll(relations);
        root.putArray("resourceFiles").addAll(resourceFiles);

        return this.getTemplateGen().compile("Controller", root);
    }
}
