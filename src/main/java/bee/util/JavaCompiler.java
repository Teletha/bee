/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package bee.util;

import static bee.util.Inputs.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import bee.Platform;
import bee.UserInterface;
import bee.api.Library;
import kiss.I;
import kiss.Signal;
import psychopath.Directory;
import psychopath.Folder;
import psychopath.Location;
import psychopath.Locator;

public class JavaCompiler {

    /** The actual java compiler. */
    private static javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    /** The user interface. */
    private final UserInterface ui;

    /** The source directories. */
    private final Folder sources = Locator.folder();

    /** The source codes. */
    private final List<JavaFileObject> codes = new ArrayList();

    /** The classpath list. */
    private final List<Location> classpaths = new ArrayList();

    /** The annotation processors. */
    private final List<Processor> processors = new ArrayList();

    /** The annotation processor classes. */
    private final List<String> processorClasses = new ArrayList();

    /** The annotation processor's locations. */
    private final Set<Location> processorClassPaths = new HashSet();

    /** The annotation processor's options. */
    private final Map<String, String> processorOptions = new HashMap();

    /** The output directory. */
    private Directory output;

    /** The source version. */
    private SourceVersion sourceVersion = SourceVersion.latest();

    /** The target version. */
    private SourceVersion targetVersion = SourceVersion.latest();

    /** The source encoding. */
    private Charset encoding = Platform.Encoding;

    /** The deprication flag. */
    private boolean deprication = false;

    /** The verbose flag. */
    private boolean verbose = false;

    /** The nowarn flag. */
    private boolean nowarn = false;

    /** The debug info flag. */
    private boolean debug = true;

    /** The error listener. */
    private DiagnosticListener<JavaFileObject> error;

    /**
     * <p>
     * Exposable constructor.
     * </p>
     */
    public JavaCompiler() {
        this(I.make(UserInterface.class));
    }

    /**
     * <p>
     * Exposable constructor.
     * </p>
     */
    public JavaCompiler(UserInterface ui) {
        this.ui = Objects.requireNonNull(ui);
    }

    /**
     * <p>
     * Set the user class path, overriding the user class path in the CLASSPATH environment
     * variable. If threr is no classpath, the user class path consists of the current directory.
     * </p>
     * 
     * @param path A classpath to add.
     */
    public void addClassPath(Location path) {
        if (path != null) {
            classpaths.add(path);
        }
    }

    /**
     * <p>
     * Set the user class path, overriding the user class path in the CLASSPATH environment
     * variable. If threr is no classpath, the user class path consists of the current directory.
     * </p>
     * 
     * @param library A classpath to add.
     */
    public void addClassPath(Library library) {
        if (library != null) {
            classpaths.add(library.getLocalJar());
        }
    }

    /**
     * <p>
     * Set the user class path, overriding the user class path in the CLASSPATH environment
     * variable. If threr is no classpath, the user class path consists of the current directory.
     * </p>
     * 
     * @param libraries A list of classpaths to add.
     */
    public void addClassPath(Set<Library> libraries) {
        if (libraries != null) {
            for (Library library : libraries) {
                addClassPath(library);
            }
        }
    }

    /**
     * Use all current classpath.
     */
    public void addCurrentClassPath() {
        for (String path : System.getProperty("java.class.path").split(File.pathSeparator)) {
            addClassPath(Locator.locate(path));
        }
    }

    /**
     * Add source code
     * 
     * @param name A class name.
     * @param code A source code to compile.
     */
    public void addSource(String name, String code) {
        codes.add(new Source(name, code));
    }

    /**
     * Add the source code directory.
     * 
     * @param directory Your source code directory.
     */
    public void addSourceDirectory(Directory directory) {
        sources.add(directory, "**.java");
    }

    /**
     * Add the source code directory.
     * 
     * @param directories Your source code directory.
     */
    public void addSourceDirectory(Signal<Directory> directories) {
        if (directories != null) {
            sources.add(directories);
        }
    }

    /**
     * Add the specified annotation processor to compile process. This method bybpasses the default
     * discovery process.
     * <p>
     * Names of the annotation processors to run. This bypasses the default discovery process.
     * 
     * @param processor
     */
    public void addProcessor(Class<? extends Processor> processor) {
        if (processor != null && !processorClasses.contains(processor.getName())) {
            processorClasses.add(processor.getName());
            processorClassPaths.add(Locator.locate(processor).absolutize());
        }
    }

    /**
     * <p>
     * Add the specified annotation processor to compile process. This method bybpasses the default
     * discovery process.
     * </p>
     * <p>
     * Names of the annotation processors to run. This bypasses the default discovery process.
     * </p>
     * 
     * @param processor
     */
    public void addProcessor(Processor processor) {
        if (processor != null && !processors.contains(processor)) {
            processors.add(processor);
        }
    }

    /**
     * <p>
     * Options to pass to annotation processors. These are not interpreted by javac directly, but
     * are made available for use by individual processors.
     * </p>
     * 
     * @param key A key name.
     * @param value A passing value.
     */
    public void addProcessorOption(String key, String value) {
        if (key != null && value != null && key.length() != 0 && value.length() != 0) {
            processorOptions.put(key, value);
        }
    }

    /**
     * Options to pass to annotation processors. These are not interpreted by javac directly, but
     * are made available for use by individual processors.
     * 
     * @param option A key-value pair.
     */
    public void addProcessorOption(Entry<String, String> option) {
        if (option != null) {
            addProcessorOption(option.getKey(), option.getValue());
        }
    }

    /**
     * Options to pass to annotation processors. These are not interpreted by javac directly, but
     * are made available for use by individual processors.
     * 
     * @param options A key-value set.
     */
    public void addProcessorOption(Map<String, String> options) {
        if (options != null) {
            for (Entry<String, String> entry : options.entrySet()) {
                addProcessorOption(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * <p>
     * Set the destination directory for class files. If a class is part of a package, javac puts
     * the class file in a subdirectory reflecting the package name, creating directories as needed.
     * For example, if you specify -d c:\myclasses and the class is called com.mypackage.MyClass,
     * then the class file is called c:\myclasses\com\mypackage\MyClass.class.
     * </p>
     * <p>
     * If you don't use this method, this compiler use in-memory storage for compiled classes.
     * </p>
     * <p>
     * Note that the directory specified by this method is not automatically added to your user
     * class path.
     * </p>
     * 
     * @param directory Your output directory. <code>null</code> value will reset to default.
     */
    public void setOutput(Path directory) {
        this.output = Locator.directory(directory);
    }

    /**
     * <p>
     * Set the destination directory for class files. If a class is part of a package, javac puts
     * the class file in a subdirectory reflecting the package name, creating directories as needed.
     * For example, if you specify -d c:\myclasses and the class is called com.mypackage.MyClass,
     * then the class file is called c:\myclasses\com\mypackage\MyClass.class.
     * </p>
     * <p>
     * If you don't use this method, this compiler use in-memory storage for compiled classes.
     * </p>
     * <p>
     * Note that the directory specified by this method is not automatically added to your user
     * class path.
     * </p>
     * 
     * @param directory Your output directory. <code>null</code> value will reset to default.
     */
    public void setOutput(Directory directory) {
        this.output = directory;
    }

    /**
     * <p>
     * Show a description of each use or override of a deprecated member or class. Without
     * deprecation, java compiler shows a summary of the source files that use or override
     * deprecated members or classes.
     * </p>
     * 
     * @param show
     */
    public void setDeprecation(boolean show) {
        this.deprication = show;
    }

    /**
     * <p>
     * Set the source file encoding name, such as EUC-JP and UTF-8. If encoding is not specified or
     * <code>null</code> is specified, the platform default converter is used.
     * </p>
     * 
     * @param encoding A charset to set.
     */
    public void setEncoding(Charset encoding) {
        if (encoding == null) {
            this.encoding = Platform.Encoding;
        } else {
            this.encoding = encoding;
        }
    }

    /**
     * Generate all debugging information, including local variables. By default, only line number
     * and source file information is generated.
     * 
     * @param debug
     */
    public void setGenerateDebugInfo(boolean debug) {
        this.debug = debug;
    }

    /**
     * Verbose output. This includes information about each class loaded and each source file
     * compiled.
     * 
     * @param verbose
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * <p>
     * Override the location of installed extensions.
     * </p>
     * 
     * @param directory
     */
    public void setExtensionDirectory(Path directory) {

    }

    /**
     * <p>
     * Disable warning messages. This has the same meaning as -Xlint:none.
     * </p>
     */
    public void setNoWarn() {
        this.nowarn = true;
    }

    /**
     * <p>
     * Set source version and target version for compiling.
     * </p>
     * 
     * @param sourceVersion
     * @param targetVersion
     */
    public void setVersion(SourceVersion sourceVersion, SourceVersion targetVersion) {
        if (sourceVersion != null) {
            this.sourceVersion = sourceVersion;
        }

        if (targetVersion != null) {
            this.targetVersion = targetVersion;
        }
    }

    /**
     * Set compiling error listener.
     * 
     * @param listener
     */
    public void setErrorListener(DiagnosticListener<JavaFileObject> listener) {
        this.error = listener;
    }

    /**
     * <p>
     * Specify the directory where to place generated source files. The directory must already
     * exist; javac will not create it. If a class is part of a package, the compiler puts the
     * source file in a subdirectory reflecting the package name, creating directories as needed.
     * For example, if you specify -s C:\mysrc and the class is called com.mypackage.MyClass, then
     * the source file will be placed in C:\mysrc\com\mypackage\MyClass.java.
     * </p>
     * 
     * @param directory
     */
    private void setGeneratedSourceDirectory(File directory) {

    }

    /**
     * <p>
     * Invoke compiler with specified options.
     * </p>
     */
    public ClassLoader compile() {
        // Build options
        ArrayList<String> options = new ArrayList();

        // =============================================
        // Option
        // =============================================
        if (verbose) {
            options.add("-verbose");
        }

        // =============================================
        // Lint
        // =============================================
        if (deprication) {
            options.add("-Xlint:deprecation");
        }

        if (nowarn) {
            options.add("-Xlint:none");
        }

        // =============================================
        // Debug
        // =============================================
        if (debug) {
            options.add("-g");
        }

        // =============================================
        // Output Directory
        // =============================================
        if (output != null) {
            // Create direcotry if needed.
            output.create();

            options.add("-d");
            options.add(output.absolutize().toString());
        }

        // =============================================
        // Annotation Processing Tools
        // =============================================
        if (processors.size() == 0 && processorClasses.size() == 0) {
            options.add("-proc:none");
        } else {
            options.add("-processor");
            options.add(String.join(",", processorClasses));
            options.add("-processorpath");
            options.add(processorClassPaths.stream().map(Location::toString).collect(Collectors.joining(",")));

            for (Entry<String, String> entry : processorOptions.entrySet()) {
                options.add("-A" + entry.getKey() + '=' + entry.getValue());
            }
        }

        // =============================================
        // Source encoding
        // =============================================
        options.add("-encoding");
        options.add(encoding.displayName());

        // =============================================
        // Source and Target Version
        // =============================================
        options.add("-source");
        options.add(normalize(sourceVersion));
        options.add("-target");
        options.add(normalize(targetVersion));

        // =============================================
        // Java Class Paths
        // =============================================
        if (classpaths.size() != 0) {
            options.add("-cp");
            options.add(classpaths.stream().map(Location::toString).collect(Collectors.joining(File.pathSeparator)));
        }

        // =============================================
        // Java Source Files
        // =============================================
        List<JavaFileObject> sources = new ArrayList(codes);

        this.sources.walkFileWithBase("**.java").to(e -> {
            if (output == null) {
                sources.add(new Source(e.ⅱ));
            } else {
                psychopath.File classFile = output.directory(e.ⅰ.relativize(e.ⅱ.parent())).file(e.ⅱ.base() + ".class");

                if (classFile.lastModifiedMilli() < e.ⅱ.lastModifiedMilli()) {
                    sources.add(new Source(e.ⅱ));
                }
            }
        });

        options.add("-sourcepath");
        options.add(String.join(File.pathSeparator, this.sources.walkFile().map(Location::toString).toList()));

        // check compiling source size
        if (sources.isEmpty()) {
            ui.info("Nothing to compile - all classes are up to date");

            return Thread.currentThread().getContextClassLoader();
        }

        // Error handling
        if (error == null) {
            error = new ErrorListener();
        }

        // Invocation
        Manager manager = new Manager(compiler.getStandardFileManager(error, Locale.getDefault(), StandardCharsets.UTF_8));

        CompilationTask task = compiler.getTask(null, manager, error, options, null, sources);

        // =============================================
        // Annotation Processing Tools
        // =============================================
        if (processors.size() != 0) {
            task.setProcessors(processors);
        }

        boolean result = task.call();

        if (result) {
            ui.info("Compile " + sources.size() + " sources.");
        } else {
            throw new Error("Compile is fail.");
        }

        return manager;
    }

    /**
     * 
     */
    private class ErrorListener implements DiagnosticListener<JavaFileObject> {
        /**
         * {@inheritDoc}
         */
        @Override
        public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
            switch (diagnostic.getKind()) {
            case ERROR:
                ui.error(diagnostic.toString());
                break;

            case MANDATORY_WARNING:
            case WARNING:
                ui.warn(diagnostic.toString());
                break;

            case NOTE:
            case OTHER:
                ui.info(diagnostic.toString());
                break;
            }
        }
    }

    /**
     * @version 2018/10/03 10:39:40
     */
    private static class Source extends SimpleJavaFileObject {

        /** The code. */
        private String code;

        /** The source file. */
        private Path file;

        /**
         * @param code
         */
        private Source(String name, String code) {
            super(URI.create("string:///" + name.replace(".", "/") + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = Objects.requireNonNull(code);
        }

        /**
         * @param file
         */
        private Source(Path file) {
            super(file.toUri(), Kind.SOURCE);
            this.file = file;
        }

        /**
         * @param file
         */
        private Source(psychopath.File file) {
            this(file.asJavaPath());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            if (file == null) {
                return code;
            } else {
                return Files.readString(file);
            }
        }
    }

    /**
     * 
     */
    private class Manager extends SecureClassLoader implements StandardJavaFileManager {

        /** The actual file manager. */
        private final StandardJavaFileManager manager;

        /** The mapping for defined classes. */
        private final Map<String, byte[]> bytes = new HashMap();

        /**
         * @param manager
         */
        public Manager(StandardJavaFileManager manager) {
            this.manager = manager;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Path asPath(FileObject file) {
            return manager.asPath(file);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(Iterable<? extends File> files) {
            return manager.getJavaFileObjectsFromFiles(files);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
            return manager.getJavaFileObjects(files);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(Iterable<String> names) {
            return manager.getJavaFileObjectsFromStrings(names);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterable<? extends JavaFileObject> getJavaFileObjects(String... names) {
            return manager.getJavaFileObjects(names);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setLocation(Location location, Iterable<? extends File> path) throws IOException {
            manager.setLocation(location, path);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setLocationForModule(javax.tools.JavaFileManager.Location location, String moduleName, Collection<? extends Path> paths)
                throws IOException {
            manager.setLocationForModule(location, moduleName, paths);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterable<? extends File> getLocation(Location location) {
            return manager.getLocation(location);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int isSupportedOption(String option) {
            return manager.isSupportedOption(option);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
            return manager.list(location, packageName, kinds, recurse);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterable<Set<javax.tools.JavaFileManager.Location>> listLocationsForModules(javax.tools.JavaFileManager.Location location)
                throws IOException {
            return manager.listLocationsForModules(location);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String inferBinaryName(Location location, JavaFileObject file) {
            return manager.inferBinaryName(location, file);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String inferModuleName(javax.tools.JavaFileManager.Location location) throws IOException {
            return manager.inferModuleName(location);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isSameFile(FileObject a, FileObject b) {
            return manager.isSameFile(a, b);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean handleOption(String current, Iterator<String> remaining) {
            return manager.handleOption(current, remaining);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasLocation(Location location) {
            return manager.hasLocation(location);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ClassLoader getClassLoader(Location location) {
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
            return manager.getJavaFileForInput(location, className, kind);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
            if (output == null) {
                return new Bytecode(className);
            } else {
                return manager.getJavaFileForOutput(location, className, kind, sibling);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
            return manager.getFileForInput(location, packageName, relativeName);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling)
                throws IOException {
            return manager.getFileForOutput(location, packageName, relativeName, sibling);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void flush() throws IOException {
            manager.flush();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() throws IOException {
            manager.close();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class<?> clazz = findLoadedClass(name);

            if (clazz == null) {
                try {
                    byte[] b = bytes.get(name);

                    if (b != null) {
                        clazz = defineClass(name, b, 0, b.length);
                    } else {
                        clazz = findClass(name);
                    }
                } catch (ClassNotFoundException e) {
                    clazz = super.loadClass(name, resolve);
                }
            }

            if (resolve) resolveClass(clazz);

            return clazz;
        }

        /**
         * @version 2010/12/18 22:45:13
         */
        private class Bytecode extends ByteArrayOutputStream implements JavaFileObject {

            /** The class name. */
            private final String name;

            /**
             * @param name
             */
            public Bytecode(String name) {
                this.name = name;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public URI toUri() {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String getName() {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public InputStream openInputStream() throws IOException {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Writer openWriter() throws IOException {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public long getLastModified() {
                return 0;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean delete() {
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Kind getKind() {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isNameCompatible(String simpleName, Kind kind) {
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public NestingKind getNestingKind() {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Modifier getAccessLevel() {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public OutputStream openOutputStream() throws IOException {
                return this;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public synchronized void close() throws IOException {
                // close stream
                super.close();

                // store byte code
                bytes.put(name, toByteArray());
            }
        }
    }
}