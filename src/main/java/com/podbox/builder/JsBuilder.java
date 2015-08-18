package com.podbox.builder;

import com.google.common.base.Optional;
import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.podbox.compiler.ClosureCompiler;
import com.podbox.parsers.ThymeleafUrlParser;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.javascript.jscomp.SourceFile.fromCode;
import static com.google.javascript.jscomp.SourceFile.fromFile;
import static com.podbox.ansi.AnsiColor.*;
import static java.util.UUID.randomUUID;
import static java.util.regex.Pattern.DOTALL;
import static org.apache.commons.lang3.StringUtils.*;

public class JsBuilder extends AbstractBuilder implements ErrorManager {

    static final Pattern SEARCH_PATTERN = Pattern.compile("<!--[\\s]*build:js[\\s]*(.*?)(?=[\\s]*-->)[\\s]*-->(.*?)(?=<!--[\\s]*endbuild[\\s]*-->)<!--[\\s]*endbuild[\\s]*-->", DOTALL);

    final LanguageMode languageMode;

    final CompilationLevel compilationLevel;

    public JsBuilder(final File sourceDirectory, final File targetDirectory, final String sourceEncoding, final LanguageMode languageMode, final CompilationLevel compilationLevel) {
        super(SEARCH_PATTERN, sourceDirectory, targetDirectory, sourceEncoding);
        this.languageMode = languageMode;
        this.compilationLevel = compilationLevel;
    }

    @Override
    protected Optional<String> compile(final String path, final Document resources) throws IOException {
        final List<SourceFile> sources = new ArrayList<>();

        final Iterator<Element> scripts = resources.select("script").iterator();
        while (scripts.hasNext()) {
            final Element script = scripts.next();

            final String sourceFileName = substringBeforeLast(script.attr("src"), "?");
            final String thSourceFileName = substringBeforeLast(script.attr("th:src"), "?");

            File sourceFile = null;

            if (startsWith(sourceFileName, JSP_CONTEXT_PATH)) {
                sourceFile = new File(sourceDirectory.getCanonicalFile(), substringAfter(sourceFileName, JSP_CONTEXT_PATH));
            }
            else if (isNotBlank(sourceFileName)) {
                sourceFile = new File(new File(sourceDirectory, path).getCanonicalFile(), sourceFileName);
            }
            else if (isNotBlank(thSourceFileName)) {
                String extracted = ThymeleafUrlParser.extractUrl(thSourceFileName);
                sourceFile = new File(sourceDirectory.getCanonicalFile(), extracted);
            }

            if (sourceFile != null) {
                logger.info("    {}─ {}{}{}", scripts.hasNext() ? '├' : '└', CYAN, isNotBlank(sourceFileName) ? sourceFileName : thSourceFileName, RESET);
                sources.add(fromFile(sourceFile, sourceCharset));
            }
            else {
                String code = "";
                for (final DataNode node : script.dataNodes()) {
                    code += node.getWholeData();
                }
                if (isNotBlank(code)) {
                    sources.add(fromCode(randomUUID().toString(), code));
                }
            }
        }

        return ClosureCompiler.compile(this, sources, languageMode, compilationLevel);
    }

    @Override
    protected String getReplacement(final String resourceName) {
        return "<script " + (startsWith(resourceName, "@{") ? "th:" : "") + "src=\"" + resourceName + "\"></script>";
    }

    @Override
    public void report(CheckLevel level, JSError error) {
        switch (level) {
            case ERROR:
                logger.error("{}{}{}", RED, error.toString(), RESET);
                throw new RuntimeException(error.toString());
            case WARNING:
                logger.warn("{}{}{}", YELLOW, error.toString(), RESET);
                break;
        }
    }

    @Override
    public void generateReport() {

    }

    @Override
    public int getErrorCount() {
        return 0;
    }

    @Override
    public int getWarningCount() {
        return 0;
    }

    @Override
    public JSError[] getErrors() {
        return new JSError[0];
    }

    @Override
    public JSError[] getWarnings() {
        return new JSError[0];
    }

    @Override
    public void setTypedPercent(double typedPercent) {

    }

    @Override
    public double getTypedPercent() {
        return 0;
    }

}
