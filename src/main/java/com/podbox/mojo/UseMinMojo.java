package com.podbox.mojo;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.podbox.UseMin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.apache.maven.plugins.annotations.LifecyclePhase.PREPARE_PACKAGE;
import static org.apache.maven.plugins.annotations.ResolutionScope.NONE;

@Mojo(
        name = "usemin",
        defaultPhase = PREPARE_PACKAGE,
        requiresDependencyResolution = NONE,
        requiresDependencyCollection = NONE
)
public class UseMinMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.sourceEncoding}", readonly = true)
    private String sourceEncoding;

    @Parameter(defaultValue = "${basedir}/src/main/webapp")
    private File sourceDirectory;

    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}")
    private File targetDirectory;

    @Parameter(defaultValue = "ECMASCRIPT5_STRICT")
    private LanguageMode languageMode;

    @Parameter(defaultValue = "SIMPLE_OPTIMIZATIONS")
    private CompilationLevel compilationLevel;

    @Parameter
    private List<String> sources;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            final UseMin useMin = new UseMin();
            useMin.setSourceEncoding(sourceEncoding);
            useMin.setSourceDirectory(sourceDirectory);
            useMin.setTargetDirectory(targetDirectory);
            useMin.setLanguageMode(languageMode);
            useMin.setCompilationLevel(compilationLevel);
            useMin.setSources(sources);
            useMin.execute();
        } catch (final IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

}
