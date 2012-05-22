/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package bee.task;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import kiss.I;
import kiss.model.ClassUtil;
import bee.api.ArtifactLocator;
import bee.api.Library;
import bee.api.Scope;
import bee.util.JarArchiver;
import bee.util.ProcessMaker;
import bee.util.ZipArchiver;

/**
 * @version 2012/04/01 9:34:01
 */
public class Exe extends Task {

    /** The builder file name. */
    private static final String exeBuilderName = "exewrap.exe";

    /** The temporary directory. */
    private final Path temporary;

    /** The location of exewrap.exe. */
    private final Path exeBuilder;

    /** The output for the generated exe file. */
    private final Path exeOutput;

    /** The output for the generated zip file. */
    private final Path zipOutput;

    /** The location for icon of exe file. */
    private Path icon;

    /**
     * 
     */
    public Exe() {
        try {
            temporary = I.locateTemporary();
            exeBuilder = temporary.resolve(exeBuilderName);
            exeOutput = temporary.resolve(project.getProduct() + ".exe");
            zipOutput = project.getOutput().resolve(project.getProduct() + "-" + project.getVersion() + ".zip");

            Files.createDirectories(temporary);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    @Command
    public Path build() {
        require(Jar.class).source();

        try {
            // search main classes
            String main = require(FindMain.class).main();

            // create temporary executable jar
            Path jar = temporary.resolve("exe.jar");

            JarArchiver archiver = new JarArchiver();
            archiver.setMainClass(Activator.class);
            archiver.add(ClassUtil.getArchive(Activator.class), "**/" + Exe.class.getSimpleName() + "$" + Activator.class.getSimpleName() + ".class");
            archiver.pack(jar);

            // unzip exewrap.exe
            I.copy(Exe.class.getResourceAsStream(exeBuilderName), Files.newOutputStream(exeBuilder), true);

            // build command line
            List<String> command = new ArrayList();
            command.add(exeBuilder.toString());
            command.add("-g");
            command.add("-o");
            command.add(exeOutput.toString());
            command.add("-j");
            command.add(jar.toString());
            command.add("-v");
            command.add(project.getVersion().replace('-', '.'));
            command.add("-t");
            command.add(project.getJavaVersion());
            command.add("-a");
            command.add("-DMainClass=" + main);

            String description = project.getDescription();

            if (description.length() != 0) {
                command.add("-d");
                command.add(description);
            }

            if (icon != null && Files.isRegularFile(icon) && icon.toString().endsWith(".ico")) {
                command.add("-i");
                command.add(icon.toAbsolutePath().toString());
            }

            // execute exewrap.exe
            ProcessMaker maker = new ProcessMaker();
            maker.setWorkingDirectory(exeBuilder.getParent());
            maker.run(command);

            // pack with dependency libraries
            ZipArchiver zip = new ZipArchiver();
            zip.add(exeOutput);
            zip.add("lib", ArtifactLocator.Jar.in(project));
            for (Library library : project.getDependency(Scope.Runtime)) {
                zip.add("lib", library.getJar());
            }
            zip.pack(zipOutput);

            return zipOutput;
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * @version 2012/04/11 13:54:42
     */
    private static final class Activator {

        /**
         * <p>
         * Application Activation Acession.
         * </p>
         * 
         * @param args
         */
        @SuppressWarnings("unused")
        static final void main(String[] args) throws Exception {
            Class clazz = Class.forName(System.getProperty("MainClass"));
            Method main = clazz.getMethod("main", String[].class);

            main.invoke(null, (Object) new String[] {});
        }
    }
}
