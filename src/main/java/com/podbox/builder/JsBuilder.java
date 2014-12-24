package com.podbox.builder;

import com.google.common.base.Optional;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.ErrorManager;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.SourceFile;
import com.podbox.compiler.ClosureCompiler;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.javascript.jscomp.CompilationLevel.SIMPLE_OPTIMIZATIONS;
import static com.google.javascript.jscomp.SourceFile.fromCode;
import static com.google.javascript.jscomp.SourceFile.fromFile;
import static java.util.UUID.randomUUID;
import static java.util.regex.Pattern.DOTALL;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

public class JsBuilder extends AbstractBuilder implements ErrorManager {

    static final Pattern SEARCH_PATTERN = Pattern.compile("<!--[\\s]*build:js[\\s]*(.*?)(?=[\\s]*-->)[\\s]*-->(.*?)(?=<!--[\\s]*endbuild[\\s]*-->)<!--[\\s]*endbuild[\\s]*-->", DOTALL);

    public JsBuilder(final Log log, final String sourceEncoding) {
        super(SEARCH_PATTERN, log, sourceEncoding);
    }

    @Override
    protected Optional<String> compile(final Document resources, final File sourceDirectory, final File targetDirectory) {
        final List<SourceFile> sources = new ArrayList<>();

        final Iterator<Element> scripts = resources.select("script").iterator();
        while (scripts.hasNext()) {
            final Element script = scripts.next();
            final String sourceFileName = substringBeforeLast(script.attr("src"), "?");

            if (isNotBlank(sourceFileName)) {
                log.info("     " + (scripts.hasNext() ? '├' : '└') + "─ " + sourceFileName);
                sources.add(fromFile(new File(sourceDirectory, sourceFileName), sourceCharset));
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

        return ClosureCompiler.compile(this, sources, SIMPLE_OPTIMIZATIONS);
    }

    @Override
    protected String getReplacement(final String resourceName) {
        return "<script src=\"" + resourceName + "\"></script>";
    }

    @Override
    public void report(CheckLevel level, JSError error) {
        switch (level) {
            case ERROR:
                log.error(error.toString());
                throw new RuntimeException(error.toString());
            case WARNING:
                log.warn(error.toString());
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
