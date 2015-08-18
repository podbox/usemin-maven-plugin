package com.podbox.builder;

import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.podbox.compiler.CssCompiler;
import com.podbox.compiler.LessCssCompiler;
import com.podbox.parsers.ThymeleafUrlParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static com.podbox.ansi.AnsiColor.CYAN;
import static com.podbox.ansi.AnsiColor.RESET;
import static java.util.regex.Pattern.DOTALL;
import static org.apache.commons.lang3.StringUtils.*;

public class CssBuilder extends AbstractBuilder {

    static final Pattern SEARCH_PATTERN = Pattern.compile("<!--[\\s]*build:css[\\s]*(.*?)(?=[\\s]*-->)[\\s]*-->(.*?)(?=<!--[\\s]*endbuild[\\s]*-->)<!--[\\s]*endbuild[\\s]*-->", DOTALL);

    static final String TEXT_LESS = "text/x-less";

    private List<String> lessOptions;

    public CssBuilder(final File sourceDirectory, final File targetDirectory, final String sourceEncoding, List<String> lessOptions) {
        super(SEARCH_PATTERN, sourceDirectory, targetDirectory, sourceEncoding);
        this.lessOptions = lessOptions;
    }

    @Override
    protected Optional<String> compile(final String path, final Document resources) throws IOException {
        final List<String> sources = new ArrayList<>();

        final Iterator<Element> stylesheets = resources.select("link[rel=stylesheet]").iterator();
        while (stylesheets.hasNext()) {
            final Element stylesheet = stylesheets.next();

            final String sourceFileName = substringBeforeLast(stylesheet.attr("href"), "?");
            final String thSourceFileName = substringBeforeLast(stylesheet.attr("th:href"), "?");

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
                logger.info("    {}─ {}{}{}", stylesheets.hasNext() ? '├' : '└', CYAN, isNotBlank(sourceFileName) ? sourceFileName : thSourceFileName, RESET);

                if (TEXT_LESS.equals(stylesheet.attr("type"))) {
                    sources.add(LessCssCompiler.compile(newArrayList(sourceFile), lessOptions).get());
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
        return "<link rel=\"stylesheet\" " + (startsWith(resourceName, "@{") ? "th:" : "") + "href=\"" + resourceName + "\" />";
    }

}
