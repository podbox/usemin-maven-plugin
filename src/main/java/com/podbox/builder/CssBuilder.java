package com.podbox.builder;

import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.podbox.compiler.CssCompiler;
import com.podbox.compiler.LessCssCompiler;
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
import static org.apache.commons.lang3.StringUtils.*;

public class CssBuilder extends AbstractBuilder {

    static final Pattern SEARCH_PATTERN = Pattern.compile("<!--[\\s]*build:css[\\s]*(.*?)(?=[\\s]*-->)[\\s]*-->(.*?)(?=<!--[\\s]*endbuild[\\s]*-->)<!--[\\s]*endbuild[\\s]*-->", DOTALL);

    static final String TEXT_LESS = "text/x-less";

    public CssBuilder(final File sourceDirectory, final File targetDirectory, final String sourceEncoding) {
        super(SEARCH_PATTERN, sourceDirectory, targetDirectory, sourceEncoding);
    }

    @Override
    protected Optional<String> compile(final String path, final Document resources) throws IOException {
        final List<String> sources = new ArrayList<>();

        final Iterator<Element> stylesheets = resources.select("link[rel=stylesheet]").iterator();
        while (stylesheets.hasNext()) {
            final Element stylesheet = stylesheets.next();

            String sourceFileName = substringBeforeLast(stylesheet.attr("href"), "?");
            final boolean jspContextPath = startsWith(sourceFileName, JSP_CONTEXT_PATH);
            if (jspContextPath) {
                sourceFileName = substringAfter(sourceFileName, JSP_CONTEXT_PATH);
            }

            if (isNotBlank(sourceFileName)) {
                logger.info("     " + (stylesheets.hasNext() ? '├' : '└') + "─ " + sourceFileName);

                final File sourceFile = jspContextPath ?
                        new File(sourceDirectory.getCanonicalFile(), sourceFileName) :
                        new File(new File(sourceDirectory, path).getCanonicalFile(), sourceFileName);

                if (TEXT_LESS.equals(stylesheet.attr("type"))) {
                    sources.add(LessCssCompiler.compile(newArrayList(sourceFile)).get());
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
