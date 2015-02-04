package com.podbox.compiler;

import com.google.common.base.Optional;
import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.javascript.jscomp.SourceFile.fromCode;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;

public final class ClosureCompiler {

    static final List<SourceFile> EXTERNS;

    static final List<String> EXTERNS_FILENAME = asList(
            "angular-1.3.js",
            "angular-1.3-http-promise.js",
            "angular-1.3-q.js",
            "jquery-1.9.js",
            "underscore-1.5.2.js"
    );

    static {
        EXTERNS = new ArrayList<>();

        final ClassLoader classLoader = currentThread().getContextClassLoader();
        try (final InputStream externsStream = classLoader.getResourceAsStream("externs.zip");
             final ZipInputStream externsZip = new ZipInputStream(externsStream)) {

            ZipEntry entry;
            while ((entry = externsZip.getNextEntry()) != null) {
                EXTERNS.add(
                        fromCode(entry.getName(), IOUtils.toString(externsZip))
                );
            }

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        try {
            for (final String filename : EXTERNS_FILENAME) {
                EXTERNS.add(fromCode(filename, IOUtils.toString(ClosureCompiler.class.getResource(filename))));
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ClosureCompiler() {
    }

    public static <T extends SourceFile> Optional<String> compile(final ErrorManager errorManager, final Collection<T> inputs, final LanguageMode languageMode, final CompilationLevel level) {
        final CompilerOptions options = new CompilerOptions();
        level.setOptionsForCompilationLevel(options);
        options.setLanguage(languageMode);

        final Compiler compiler = new Compiler(errorManager);
        final Result result = compiler.compile(EXTERNS, newArrayList(inputs), options);
        return fromNullable(result.success ? compiler.toSource() : null);
    }

}
