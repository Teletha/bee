/*
 * Copyright (C) 2022 The BEE Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package bee.task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import bee.Platform;
import bee.Task;
import bee.api.Command;
import bee.api.License;
import bee.api.Scope;
import bee.api.VCS;
import bee.util.Inputs;
import kiss.I;
import psychopath.File;

public class CI extends Task {

    @Command(defaults = true, value = "Setup CI/CD")
    public void setup() {
        VCS vcs = project.getVersionControlSystem();

        if (vcs == null) {
            ui.info("No version control system.");
        } else {
            ui.info("Detect version control system.");

            if (vcs.name().equals("github")) {
                require(CI::github);
            }
        }
    }

    @Command("Generate CI/CD configuration files for GitHub.")
    public void github() {
        require(CI::gitignore, CI::jitpack);

        String build = """
                name: Build and Deploy

                on:
                  push:
                    branches: [master, main]
                  pull_request:
                    branches: [master, main]
                  workflow_dispatch:

                jobs:
                  build:
                    runs-on: ubuntu-latest
                    steps:
                    - name: Check out repository
                      uses: actions/checkout@v2

                    - name: Set up JDK
                      uses: actions/setup-java@v2
                      with:
                        distribution: zulu
                        java-version: %s

                    - name: Build artifact and site
                      run: |
                        if [ -e "bee" ]; then
                          source bee install doc:site
                        else
                          version=$(curl -SsL https://git.io/stable-bee)
                          curl -SsL -o bee-${version}.jar https://jitpack.io/com/github/teletha/bee/${version}/bee-${version}.jar
                          java -javaagent:bee-${version}.jar -cp bee-${version}.jar bee.Bee install doc:site
                        fi

                    - name: Deploy site
                      uses: peaceiris/actions-gh-pages@v3
                      with:
                        github_token: ${{ secrets.GITHUB_TOKEN }}
                        publish_dir: target/site

                    - name: Request Releasing
                      uses: GoogleCloudPlatform/release-please-action@v2
                      with:
                        release-type: simple
                        package-name: %s
                """;

        String version = Inputs.normalize(project.getJavaSourceVersion());

        // The output result from the Release-Please action contains a newline,
        // so we will adjust it.
        makeFile("version.txt", List.of(project.getVersion(), "")).text(o -> o.replaceAll("\\R", "\n"));
        makeFile(".github/workflows/build.yml", String.format(build, version, project.getProduct()));
        makeLicenseFile();
        makeReadMeFile();

        // delete old settings
        deleteFile(".github/workflows/java-ci-with-maven.yml");
        deleteFile(".github/workflows/release-please.yml");
    }

    /**
     * Create license file if needed
     */
    private void makeLicenseFile() {
        License license = project.license();

        if (license == null) {
            return; // not specified
        }

        if (checkFile("LICENSE.txt") || checkFile("LICENSE.md") || checkFile("LICENSE.rst")) {
            return; // already exists
        }

        makeFile("LICENSE.txt", license.text(false));
    }

    /**
     * Create README file if needed
     */
    private void makeReadMeFile() {
        makeFile("README.md", I
                .express("""
                        <p align="center">
                            <a href="https://docs.oracle.com/en/java/javase/{java}/"><img src="https://img.shields.io/badge/Java-Release%20{java}-green"/></a>
                            <span>&nbsp;</span>
                            <a href="https://jitpack.io/#{owner}/{repo}"><img src="https://img.shields.io/jitpack/v/{name}/{owner}/{repo}?label=Repository&color=green"></a>
                            <span>&nbsp;</span>
                            <a href="https://{owner}.github.io/{repo}"><img src="https://img.shields.io/website.svg?down_color=red&down_message=CLOSE&label=Official%20Site&up_color=green&up_message=OPEN&url=https%3A%2F%2F{owner}.github.io%2F{repo}"></a>
                        </p>


                        ## Summary
                        {description}
                        <p align="right"><a href="#top">back to top</a></p>


                        ## Usage
                        {usage}
                        <p align="right"><a href="#top">back to top</a></p>


                        ## Prerequisites
                        {ProductName} runs on all major operating systems and requires only [Java version {java}](https://docs.oracle.com/en/java/javase/{java}/) or later to run.
                        To check, please run `java -version` from the command line interface. You should see something like this:
                        ```
                        > java -version
                        openjdk version "16" 2021-03-16
                        OpenJDK Runtime Environment (build 16+36-2231)
                        OpenJDK 64-Bit Server VM (build 16+36-2231, mixed mode, sharing)
                        ```
                        <p align="right"><a href="#top">back to top</a></p>

                        ## Install
                        For any code snippet below, please substitute the version given with the version of {ProductName} you wish to use.
                        #### [Maven](https://maven.apache.org/)
                        Add JitPack repository at the end of repositories element in your build.xml:
                        ```xml
                        <repository>
                            <id>jitpack.io</id>
                            <url>https://jitpack.io</url>
                        </repository>
                        ```
                        Add it into in the dependencies element like so:
                        ```xml
                        <dependency>
                            <groupId>{group}</groupId>
                            <artifactId>{product}</artifactId>
                            <version>{version}</version>
                        </dependency>
                        ```
                        #### [Gradle](https://gradle.org/)
                        Add JitPack repository at the end of repositories in your build.gradle:
                        ```gradle
                        repositories {
                            maven }} url "https://jitpack.io" }
                        }
                        ```
                        Add it into the dependencies section like so:
                        ```gradle
                        dependencies {
                            implementation '{group}:{product}:{version}'
                        }
                        ```
                        #### [SBT](https://www.scala-sbt.org/)
                        Add JitPack repository at the end of resolvers in your build.sbt:
                        ```scala
                        resolvers += "jitpack" at "https://jitpack.io"
                        ```
                        Add it into the libraryDependencies section like so:
                        ```scala
                        libraryDependencies += "{group}" % "{product}" % "{version}"
                        ```
                        #### [Leiningen](https://leiningen.org/)
                        Add JitPack repository at the end of repositories in your project.clj:
                        ```clj
                        :repositories [["jitpack" "https://jitpack.io"]]
                        ```
                        Add it into the dependencies section like so:
                        ```clj
                        :dependencies [[{group}/{product} "{version}"]]
                        ```
                        #### [Bee](https://teletha.github.io/bee)
                        Add it into your project definition class like so:
                        ```java
                        require("{group}", "{product}", "{version}");
                        ```
                        <p align="right"><a href="#top">back to top</a></p>


                        ## Contributing
                        Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.
                        If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
                        Don't forget to give the project a star! Thanks again!

                        1. Fork the Project
                        2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
                        3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
                        4. Push to the Branch (`git push origin feature/AmazingFeature`)
                        5. Open a Pull Request

                        The overwhelming majority of changes to this project don't add new features at all. Optimizations, tests, documentation, refactorings -- these are all part of making this product meet the highest standards of code quality and usability.
                        Contributing improvements in these areas is much easier, and much less of a hassle, than contributing code for new features.

                        ### Bug Reports
                        If you come across a bug, please file a bug report. Warning us of a bug is possibly the most valuable contribution you can make to {ProductName}.
                        If you encounter a bug that hasn't already been filed, [please file a report](https://github.com/{owner}/{repo}/issues/new) with an [SSCCE](http://sscce.org/) demonstrating the bug.
                        If you think something might be a bug, but you're not sure, ask on StackOverflow or on [{product}-discuss](https://github.com/{owner}/{repo}/discussions).
                        <p align="right"><a href="#top">back to top</a></p>


                        ## Dependency
                        {ProductName} depends on the following products on runtime.
                        {#dependencies}
                        * [{.}](https://mvnrepository.com/artifact/{group}/{name}/{version})
                        {/dependencies}
                        {^dependencies}
                        * No Dependency
                        {/dependencies}

                        {ProductName} depends on the following products on test.
                        {#testDependencies}
                        * [{.}](https://mvnrepository.com/artifact/{group}/{name}/{version})
                        {/testDependencies}
                        {^testDependencies}
                        * No Dependency
                        {/testDependencies}
                        <p align="right"><a href="#top">back to top</a></p>


                        ## License
                        {license}
                        <p align="right"><a href="#top">back to top</a></p>
                        """, new Object[] {
                        project}, (m, o, e) -> {
                            switch (e) {
                            case "ProductName":
                                return Inputs.capitalize(project.getProduct());

                            case "java":
                                return Inputs.normalize(project.getJavaClassVersion());

                            case "owner":
                                return project.getVersionControlSystem().owner;

                            case "repo":
                                return project.getVersionControlSystem().repo;

                            case "name":
                                return project.getVersionControlSystem().name();

                            case "dependencies":
                                return new ArrayList(project.getDependency(Scope.Runtime));

                            case "testDependencies":
                                return new ArrayList(project.getDependency(Scope.Test));

                            case "license":
                                return project.license().text(false).stream().collect(Collectors.joining(Platform.EOL));

                            default:
                                return null;
                            }
                        })
                .replace("}}", "{"));
    }

    @Command("Generate CI/CD configuration files for JitPack.")
    public void jitpack() {
        String sourceVersion = Inputs.normalize(project.getJavaSourceVersion());

        makeFile("jitpack.yml", String.format("""
                jdk:
                  - openjdk%s

                before_install: |
                  source ~/.sdkman/bin/sdkman-init.sh
                  sdk install java %s-open
                  source ~/.sdkman/bin/sdkman-init.sh

                install: |
                  if [ -e "bee" ]; then
                    source bee install pom
                  else
                    version=$(curl -SsL https://git.io/stable-bee)
                    curl -SsL -o bee-${version}.jar https://jitpack.io/com/github/teletha/bee/${version}/bee-${version}.jar
                    java -javaagent:bee-${version}.jar -cp bee-${version}.jar bee.Bee install pom
                  fi
                """, sourceVersion, sourceVersion));
    }

    @Command("Generate .gitignore file.")
    public void gitignore() {
        File ignore = project.getRoot().file(".gitignore");

        makeFile(ignore, update(ignore.lines().toList()));
    }

    /**
     * Update gitignore configuration.
     * 
     * @param lines Lines to update.
     * @return An updated lines.
     */
    List<String> update(List<String> lines) {
        StringJoiner uri = new StringJoiner(",", "https://www.gitignore.io/api/", "").add("Java").add("Maven").add("Windows").add("Linux");

        // IDE
        for (IDESupport ide : I.find(IDESupport.class)) {
            uri.add(ide.toString());
        }

        LinkedList<String> updated = I.http(uri.toString(), String.class)
                .waitForTerminate()
                .flatArray(rule -> rule.split("\\R"))
                .startWith(".*", "!/.gitignore", "!/.github")
                .startWith(lines)
                .take(HashSet::new, (set, v) -> v.isBlank() || set.add(v))
                .toCollection(new LinkedList());

        while (updated.peekLast().isBlank()) {
            updated.pollLast();
        }
        return updated;
    }

    /**
     * 
     */
    @SuppressWarnings("serial")
    static class UsageList extends ArrayList<Usage> {

        /**
         * Create new {@link Usage}.
         * 
         * @return
         */
        private Usage createNewUsage() {
            Usage usage = new Usage();
            add(usage);
            return usage;
        }

        /**
         * Parse source.
         * 
         * @param source
         * @return
         */
        static UsageList parse(String source) {
            UsageList list = new UsageList();

            int start = 0;
            int end = 0;
            String indent = "";
            Usage usage = null;

            String[] lines = source.split("[\\r\\n]+");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];

                if (line.strip().startsWith("@Test")) {
                    indent = line.substring(0, line.indexOf("@Test"));
                    start = i;
                    usage = list.createNewUsage();
                } else if (start != 0) {
                    if (line.equals(indent.concat("}"))) {
                        start = 0;
                    } else {
                        usage.lines.add(line);
                    }
                }
            }
            return list;
        }
    }

    /**
     * 
     */
    static class Usage {

        List<String> lines = new ArrayList();

        String text() {
            return lines.stream().collect(Collectors.joining("\\r\\n"));
        }
    }
}