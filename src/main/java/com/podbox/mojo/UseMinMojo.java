package com.podbox.mojo;

import com.google.common.io.Files;
import com.podbox.builder.CdnBuilder;
import com.podbox.builder.CssBuilder;
import com.podbox.builder.JsBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.io.Files.*;
import static java.nio.charset.Charset.forName;
import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.maven.plugins.annotations.LifecyclePhase.PREPARE_PACKAGE;
import static org.apache.maven.plugins.annotations.ResolutionScope.NONE;
import static org.jsoup.Jsoup.parse;

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

    @Parameter
    private List<String> sources;

    @Override
    public void execute() throws MojoExecutionException {
        if (sources == null || sources.isEmpty()) return;

        final CssBuilder cssBuilder = new CssBuilder(getLog(), sourceEncoding);
        final JsBuilder jsBuilder = new JsBuilder(getLog(), sourceEncoding);
        final CdnBuilder cdnBuilder = new CdnBuilder(getLog(), sourceEncoding);

        for (final String source : sources) {
            getLog().info("Processing " + source);

            final boolean isHtml = endsWith(source, ".html");
            final File sourceFile = new File(sourceDirectory, source);
            final File targetFile = new File(targetDirectory, source);

            final File parentSourceDirectory = sourceFile.getParentFile();
            final String path = substringAfter(parentSourceDirectory.getAbsolutePath(), sourceDirectory.getAbsolutePath());
            final File parentTargetDirectory = new File(targetDirectory, path);

            try {
                String html;
                if (isHtml) {
                    final Document document = parse(sourceFile, sourceEncoding);
                    html = document.normalise().outerHtml();
                }
                else {
                    html = Files.toString(sourceFile, forName(sourceEncoding));
                }

                html = cssBuilder.usemin(parentSourceDirectory, parentTargetDirectory, html);
                html = jsBuilder.usemin(parentSourceDirectory, parentTargetDirectory, html);
                if (isHtml) {
                    html = cdnBuilder.usemin(parentSourceDirectory, parentTargetDirectory, html);
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
