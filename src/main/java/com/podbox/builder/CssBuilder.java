package com.podbox.builder;

import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.podbox.compiler.CssCompiler;
import com.podbox.compiler.LessCssCompiler;
import org.apache.maven.plugin.logging.Log;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.regex.Pattern.DOTALL;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

public class CssBuilder extends AbstractBuilder {

    static final Pattern SEARCH_PATTERN = Pattern.compile("<!--[\\s]*build:css[\\s]*(.*?)(?=[\\s]*-->)[\\s]*-->(.*?)(?=<!--[\\s]*endbuild[\\s]*-->)<!--[\\s]*endbuild[\\s]*-->", DOTALL);

    static final String TEXT_LESS = "text/x-less";

    public CssBuilder(final Log log, final String sourceEncoding) {
        super(SEARCH_PATTERN, log, sourceEncoding);
    }

    @Override
    protected Optional<String> compile(final Document resources, final File sourceDirectory, final File targetDirectory) throws IOException {
        final List<String> sources = new ArrayList<>();

        final Iterator<Element> stylesheets = resources.select("link[rel=stylesheet]").iterator();
        while (stylesheets.hasNext()) {
            final Element stylesheet = stylesheets.next();
            final String sourceFileName = substringBeforeLast(stylesheet.attr("href"), "?");

            if (isNotBlank(sourceFileName)) {
                final File sourceFile = new File(sourceDirectory, sourceFileName);
                log.info("     " + (stylesheets.hasNext() ? '├' : '└') + "─ " + sourceFileName);

                if (TEXT_LESS.equals(stylesheet.attr("type"))) {
                    sources.add(LessCssCompiler.compile(log, newArrayList(sourceFile)).get());
                }
                else {
                    sources.add(Files.toString(sourceFile, sourceCharset));
                }
            }
        }

        return CssCompiler.compile(sources);
    }

    @Override
    protected String getReplacement(final String resourceName) {
        return "<link rel=\"stylesheet\" href=\"" + resourceName + "\">";
    }

}
