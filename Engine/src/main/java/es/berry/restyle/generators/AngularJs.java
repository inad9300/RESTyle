package es.berry.restyle.generators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.berry.restyle.core.Generator;
import es.berry.restyle.core.TemplateGen;
import es.berry.restyle.logging.Log;
import es.berry.restyle.logging.Logger;
import es.berry.restyle.specification.SpecObjectMapper;
import es.berry.restyle.specification.generated.Field;
import es.berry.restyle.specification.generated.Resource;
import es.berry.restyle.specification.generated.Spec;
import es.berry.restyle.utils.Strings;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Plugin to create a REST client based on the AngularJS 1 JavaScript framework.
 */
public class AngularJs extends Generator {

    private final Path clientOut;
    private final Path pagesOut;
    private final Path viewsOut;
    private final Path servicesOut;

    private final TemplateGen htmlTemplateGen;

    private static final String APP_MODULE = "app";

    private static final Logger log = Log.getChain();

    public AngularJs(Spec spec, JsonNode specNode, File outputDir) throws IOException {
        super(spec, specNode, outputDir);

        this.setTemplateGen(new TemplateGen(AngularJs.class, "js"));
        // this.prevGeneratorMustImplement(X.class);

        this.htmlTemplateGen = new TemplateGen(AngularJs.class, "html");

        final File finalDir = new File(this.getOut().getAbsolutePath() + "/angular-1-client");

        // Remove previously generated output
        log.info("· Cleaning up existing artifacts...");
        FileUtils.deleteDirectory(finalDir);

        // Store references to helpful paths
        this.clientOut = finalDir.toPath();
        this.pagesOut = new File(this.clientOut + "/pages").toPath();
        this.viewsOut = new File(this.clientOut + "/common/views").toPath();
        this.servicesOut = new File(this.clientOut + "/common/services").toPath();
    }

    /**
     * Main method.
     */
    @Override
    public void generate() {
        try {
            log.info("· Generating initial file structure...");
            getInitialConfig();

            log.info("· Creating services, views and controllers...");
            for (Resource res : this.getSpec().getResources()) {
                Strings.toFile(doServicePart(res), this.servicesOut + "/" + Strings.studly(res.getPlural()) + "Srv.js");

                final String controllerOut = this.clientOut + "/pages/" + Strings.dashify(res.getPlural()) + "/";
                Strings.toFile(doControllerPart(res), controllerOut + getControllerName(res) + ".js");

                Strings.toFile(doListViewPart(res), controllerOut + getListView(res));
                Strings.toFile(doSingleViewPart(res), controllerOut + getSingleView(res));
            }
        } catch (IOException e) {
            log.error("Error generating some file in plugin " + this.getClass().getSimpleName(), e);
        }
    }

    /**
     * Set up an initial folder structure ready to hold an AngularJS application.
     */
    private void getInitialConfig() throws IOException {
        // Copy seed project
        FileUtils.copyDirectory(
                new File(this.getTemplateGen().getBaseDir() + "ng-seed"),
                this.clientOut.toFile()
        );

        final ObjectNode node = SpecObjectMapper.getInstance().createObjectNode();
        node.put("appModule", APP_MODULE);
        node.put("apiUrl", this.getSpec().getBaseUrl());
        node.putArray("resources").addAll(getResourcesInfo());

        Strings.toFile(
                this.getTemplateGen().compile("app", node),
                this.clientOut + "/app.js"
        );

        Strings.toFile(
                this.htmlTemplateGen.compile("index", node),
                this.clientOut + "/index.html"
        );

        Strings.toFile(
                this.htmlTemplateGen.compile("nav", node),
                this.viewsOut + "/nav.html"
        );

        String userSrv = "";

        for (Resource res : this.getSpec().getResources())
            if (res.getIsUser())
                userSrv = getServiceName(res);

        node.put("userSrv", userSrv);

        Strings.toFile(
                this.getTemplateGen().compile("HttpInterceptor", node),
                this.servicesOut + "/HttpInterceptor.js"
        );

        Strings.toFile(
                this.getTemplateGen().compile("HomeCtrl", node),
                this.pagesOut + "/home/HomeCtrl.js"
        );

        for (JsonNode n : getResourcesInfo())
            new File(this.clientOut + "/pages/" + n.get("folder").asText()).mkdirs();
    }

    private static String getControllerName(Resource res) {
        return Strings.studly(res.getPlural()) + "Ctrl";
    }

    private static String getServiceName(Resource res) {
        return Strings.studly(res.getPlural()) + "Srv";
    }

    private static String getListView(Resource res) {
        return Strings.dashify(res.getPlural()) + "-list.html";
    }

    private static String getSingleView(Resource res) {
        return Strings.dashify(res.getPlural()) + "-form.html";
    }

    /**
     * Single method responsible for emitting all the information about a resource. Some of it won't be used in some
     * templates, but a lot of repetition is avoided this way.
     */
    private ObjectNode getResourceInfo(Resource res) {
        final String dashedName = Strings.dashify(res.getPlural());
        final String niceName = Strings.ucFirst(dashedName.replaceAll("[-_]", " "));

        final ObjectNode node = SpecObjectMapper.getInstance().createObjectNode()
                .put("appModule", APP_MODULE)
                .put("niceName", niceName)
                .put("plural", res.getPlural())
                .put("controller", getControllerName(res))
                .put("service", getServiceName(res))
                .put("path", dashedName)
                .put("folder", dashedName)
                .put("isUser", res.getIsUser())
                .put("listView", getListView(res))
                .put("singleView", getSingleView(res));

        final ArrayNode fields = SpecObjectMapper.getInstance().createArrayNode();
        for (Field f : res.getFields())
            fields.add(SpecObjectMapper.getInstance().createObjectNode()
                    .put("name", f.getName())
                    .put("niceName", Strings.ucFirst(Strings.dashify(f.getName()).replaceAll("[-_]", " ")))
                    .put("description", f.getDescription())
                    .put("attrs", AngularJsHelper.getHtmlInputAttrs(f))
                    // .put("isFile", f.getType().equals(Field.Type.FILE))
                    // .put("isHidden", f.getEncrypted())
            );
        node.putArray("fields").addAll(fields);

        return node;
    }

    // Cache reference
    private ArrayNode resourcesInfo = null;

    /**
     * Shortcut to collect the information about all the resources.
     */
    private ArrayNode getResourcesInfo() {
        if (resourcesInfo != null)
            return resourcesInfo;

        final ArrayNode resources = SpecObjectMapper.getInstance().createArrayNode();

        for (Resource res : this.getSpec().getResources())
            resources.add(getResourceInfo(res));

        resourcesInfo = resources;
        return resources;
    }

    private String doServicePart(Resource res) {
        return this.getTemplateGen().compile("Service", getResourceInfo(res));
    }

    private String doControllerPart(Resource res) {
        return this.getTemplateGen().compile("Controller", getResourceInfo(res));
    }

    private String doListViewPart(Resource res) {
        return this.htmlTemplateGen.compile("view-list", getResourceInfo(res));
    }

    private String doSingleViewPart(Resource res) {
        return this.htmlTemplateGen.compile("view-single", getResourceInfo(res));
    }
}
