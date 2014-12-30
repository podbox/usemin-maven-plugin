package com.podbox.mojo;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.podbox.builder.CdnBuilder;
import com.podbox.builder.CssBuilder;
import com.podbox.builder.FileRevBuilder;
import com.podbox.builder.JsBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.io.Files.*;
import static java.nio.charset.Charset.forName;
import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.substringAfter;
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

    @Parameter(defaultValue = "src/main/webapp")
    private File sourceDirectory;

    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}")
    private File targetDirectory;

    @Parameter(defaultValue = "ECMASCRIPT5_STRICT")
    private LanguageMode languageMode;

    @Parameter(defaultValue = "SIMPLE_OPTIMIZATIONS")
    private CompilationLevel compilationLevel;

    @Parameter
    private List<String> sources;

    public void setSourceEncoding(final String sourceEncoding) {
        this.sourceEncoding = sourceEncoding;
    }

    public void setSourceDirectory(final String sourceDirectory) {
        this.sourceDirectory = new File(sourceDirectory);
    }

    public void setTargetDirectory(final String targetDirectory) {
        this.targetDirectory = new File(targetDirectory);
    }

    public void setLanguageMode(final String languageMode) {
        this.languageMode = LanguageMode.fromString(languageMode);
    }

    public void setCompilationLevel(final String compilationLevel) {
        this.compilationLevel = CompilationLevel.valueOf(compilationLevel);
    }

    public void setSources(final List<String> sources) {
        this.sources = ImmutableList.copyOf(sources);
    }

    public void setSources(final String[] sources) {
        this.sources = ImmutableList.copyOf(sources);
    }

    @Override
    public void execute() throws MojoExecutionException {
        if (sources == null || sources.isEmpty()) return;

        final CssBuilder cssBuilder = new CssBuilder(getLog(), sourceDirectory, targetDirectory, sourceEncoding);
        final JsBuilder jsBuilder = new JsBuilder(getLog(), sourceDirectory, targetDirectory, sourceEncoding, languageMode, compilationLevel);
        final FileRevBuilder fileRevBuilder = new FileRevBuilder(getLog(), sourceDirectory, targetDirectory, sourceEncoding);
        final CdnBuilder cdnBuilder = new CdnBuilder(getLog(), sourceDirectory, targetDirectory, sourceEncoding);

        for (final String source : sources) {
            getLog().info("Processing " + source);

            final boolean isHtml = endsWith(source, ".html");
            final File sourceFile = new File(sourceDirectory, source);
            final File targetFile = new File(targetDirectory, source);

            final File parentSourceDirectory = sourceFile.getParentFile();
            final String path = substringAfter(parentSourceDirectory.getAbsolutePath(), sourceDirectory.getAbsolutePath());

            try {
                String html = Files.toString(sourceFile, forName(sourceEncoding));

                html = cssBuilder.usemin(path, html);
                html = jsBuilder.usemin(path, html);
                html = fileRevBuilder.usemin(path, html);
                if (isHtml) {
                    html = cdnBuilder.usemin(path, html);
                }

                createParentDirs(targetFile);
                touch(targetFile);
                write(html, targetFile, forName(sourceEncoding));
            } catch (final IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

}
