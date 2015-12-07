package com.podbox;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.podbox.builder.CdnBuilder;
import com.podbox.builder.CssBuilder;
import com.podbox.builder.FileRevBuilder;
import com.podbox.builder.JsBuilder;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.*;
import static com.google.javascript.jscomp.CompilationLevel.SIMPLE_OPTIMIZATIONS;
import static com.google.javascript.jscomp.CompilerOptions.LanguageMode.ECMASCRIPT5_STRICT;
import static java.nio.charset.Charset.forName;
import static org.apache.commons.lang3.StringUtils.*;
import static org.slf4j.LoggerFactory.getLogger;

public class UseMin {

    private final Logger logger = getLogger(UseMin.class);

    private String sourceEncoding = UTF_8.name();

    private File sourceDirectory;

    private File targetDirectory;

    private LanguageMode languageMode = ECMASCRIPT5_STRICT;

    private CompilationLevel compilationLevel = SIMPLE_OPTIMIZATIONS;

    private List<String> sources;

    private List<String> lessOptions;

    private String fileRevOption;

    public UseMin() {
        sources = Collections.emptyList();
        lessOptions = Collections.emptyList();
    }

    public void setSourceEncoding(final String sourceEncoding) {
        this.sourceEncoding = sourceEncoding;
    }

    public void setSourceDirectory(final File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public void setTargetDirectory(final File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public void setLanguageMode(final String languageMode) {
        this.languageMode = LanguageMode.valueOf(languageMode);
    }

    public void setLanguageMode(final LanguageMode languageMode) {
        this.languageMode = languageMode;
    }

    public void setCompilationLevel(final String compilationLevel) {
        this.compilationLevel = CompilationLevel.valueOf(compilationLevel);
    }

    public void setCompilationLevel(final CompilationLevel compilationLevel) {
        this.compilationLevel = compilationLevel;
    }

    public void setSources(final List<String> sources) {
        this.sources = sources != null ? ImmutableList.copyOf(sources) : Collections.<String>emptyList();
    }

    public void setSources(final String[] sources) {
        this.sources = sources != null ? ImmutableList.copyOf(sources) : Collections.<String>emptyList();
    }

    public void setLessOptions(List<String> lessOptions) {
        this.lessOptions = lessOptions != null ? ImmutableList.copyOf(lessOptions) : Collections.<String>emptyList();
    }

    public List<String> getLessOptions() {
        return lessOptions;
    }

    public String getFileRevOption() {
        return fileRevOption;
    }

    public void setFileRevOption(String fileRevOption) {
        this.fileRevOption = fileRevOption;
    }

    public void execute() throws IOException {
        if (sources == null || sources.isEmpty()) return;

        final CssBuilder cssBuilder = new CssBuilder(sourceDirectory, targetDirectory, sourceEncoding, getLessOptions(), fileRevOption);
        final JsBuilder jsBuilder = new JsBuilder(sourceDirectory, targetDirectory, sourceEncoding, languageMode, compilationLevel, fileRevOption);
        final FileRevBuilder fileRevBuilder = new FileRevBuilder(sourceDirectory, targetDirectory, sourceEncoding, fileRevOption);
        final CdnBuilder cdnBuilder = new CdnBuilder(sourceDirectory, targetDirectory, sourceEncoding, fileRevOption);

        for (final String source : sources) {
            logger.info("Processing {}", source);
            if (lessOptions != null && !lessOptions.isEmpty()) {
                logger.info("    Using less options: ");
                for (String lessOption : lessOptions) {
                    logger.info("        " + lessOption);
                }
            }
            logger.info(EMPTY);

            final boolean isHtml = endsWith(source, ".html");
            final File sourceFile = new File(sourceDirectory, source);
            final File targetFile = new File(targetDirectory, source);

            final File parentSourceDirectory = sourceFile.getParentFile();
            final String path = substringAfter(parentSourceDirectory.getAbsolutePath(), sourceDirectory.getAbsolutePath());

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
        }
    }
}
