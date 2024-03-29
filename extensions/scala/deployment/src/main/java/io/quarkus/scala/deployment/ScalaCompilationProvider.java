package io.quarkus.scala.deployment;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import io.quarkus.deployment.dev.CompilationProvider;
import io.quarkus.paths.PathCollection;
import scala.collection.JavaConverters;
import scala.tools.nsc.Global;
import scala.tools.nsc.Settings;

public class ScalaCompilationProvider implements CompilationProvider {

    @Override
    public String getProviderKey() {
        return "scala";
    }

    @Override
    public Set<String> handledExtensions() {
        return Collections.singleton(".scala");
    }

    @Override
    public void compile(Set<File> files, Context context) {
        Settings settings = new Settings();
        context.getClasspath().stream()
                .map(File::getAbsolutePath)
                .forEach(f -> settings.classpath().append(f));
        settings.outputDirs().add(context.getSourceDirectory().getAbsolutePath(),
                context.getOutputDirectory().getAbsolutePath());
        try (Global g = new Global(settings)) {
            Global.Run run = g.new Run();
            Set<String> fileSet = files.stream()
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toSet());
            run.compile(JavaConverters.asScalaSet(fileSet).toList());
        }
    }

    @Override
    public Path getSourcePath(Path classFilePath, PathCollection sourcePaths, String classesPath) {
        return classFilePath;
    }
}
