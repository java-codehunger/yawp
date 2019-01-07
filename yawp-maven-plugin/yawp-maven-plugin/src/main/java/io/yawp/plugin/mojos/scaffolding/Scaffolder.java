package io.yawp.plugin.mojos.scaffolding;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.maven.plugin.logging.Log;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

public abstract class Scaffolder {

    private static final String SOURCE_MAIN = "src/main/${lang}";

    private static final String SOURCE_TEST = "src/test/${lang}";

    private static final String MODELS_FOLDER = "models";

    private Log log;

    private String lang;

    protected EndpointNaming naming;

    protected String yawpPackage;

    public Scaffolder(Log log, String lang, String yawpPackage, String model) {
        this.log = log;
        this.lang = lang;
        this.yawpPackage = yawpPackage;
        this.naming = new EndpointNaming(lang, model);
    }

    // TODO: remove this
    public Scaffolder(Log log, String yawpPackage, String model) {
        this.log = log;
        this.yawpPackage = yawpPackage;
        this.naming = new EndpointNaming(model);
    }

    public void createTo(String baseDir) {
        log.info("Scaffolding to " + baseDir);
        execute(baseDir);
    }

    protected abstract void execute(String baseDir);

    private String parse(String scaffoldingTemplate) {
        VelocityContext context = new VelocityContext();
        context.put("yawpPackage", yawpPackage);
        context.put("endpoint", naming);
        return parseTemplate(scaffoldingTemplate, context);
    }

    private String parseTemplate(String scaffoldingTemplate, VelocityContext context) {
        VelocityEngine engine = createVelocityEngine();
        Template template = engine.getTemplate(scaffoldingTemplate);

        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        return writer.toString();
    }

    private VelocityEngine createVelocityEngine() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        return ve;
    }

    private File getFile(String baseDir, String relativeFilename) throws ScaffoldAlreadyExistsException {
        String filename = String.format("%s/%s", baseDir, relativeFilename);

        File file = new File(filename);

        if (file.exists()) {
            throw new ScaffoldAlreadyExistsException();
        }

        file.getParentFile().mkdirs();
        return file;
    }

    private String yawpPackageDir() {
        return yawpPackage.replaceAll("\\.", "/");
    }

    private void createFile(String baseDir, String relativeFilename, String content) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(getFile(baseDir, relativeFilename)));
            pw.print(content);
            log.info(String.format("Scaffold %s created.", relativeFilename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ScaffoldAlreadyExistsException e) {
            log.info(String.format("Scaffold %s already exists, skipping.", relativeFilename));
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    private String sourceMain() {
        return String.format("%s/%s/%s", naming.parsePath(SOURCE_MAIN), yawpPackageDir(), MODELS_FOLDER);
    }

    private String sourceTest() {
        return String.format("%s/%s/%s", naming.parsePath(SOURCE_TEST), yawpPackageDir(), MODELS_FOLDER);
    }

    protected void sourceMain(String baseDir, String filename, String modelTemplate) {
        String relativeFilename = String.format("%s/%s", sourceMain(), filename);
        createFile(baseDir, relativeFilename, parse(modelTemplate));
    }

    protected void sourceTest(String baseDir, String filename, String modelTemplate) {
        String relativeFilename = String.format("%s/%s", sourceTest(), filename);
        createFile(baseDir, relativeFilename, parse(modelTemplate));
    }

}
