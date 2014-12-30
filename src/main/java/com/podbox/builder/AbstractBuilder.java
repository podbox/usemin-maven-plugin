package com.podbox.builder;

import com.google.common.base.Optional;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.io.Files.*;
import static com.podbox.ansi.AnsiColor.*;
import static com.podbox.compiler.FileRevCompiler.filerev;
import static java.nio.charset.Charset.forName;
import static java.util.regex.Matcher.quoteReplacement;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.quote;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.jsoup.Jsoup.parse;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractBuilder {

    protected static final String JSP_CONTEXT_PATH = "${pageContext.request.contextPath}/";

    protected final Logger logger = getLogger(getClass());

    protected final Pattern searchPattern;

    protected final File sourceDirectory;

    protected final File targetDirectory;

    protected final String sourceEncoding;

    protected final Charset sourceCharset;

    protected AbstractBuilder(final Pattern searchPattern, final File sourceDirectory, final File targetDirectory, final String sourceEncoding) {
        this.searchPattern = searchPattern;
        this.sourceDirectory = sourceDirectory;
        this.targetDirectory = targetDirectory;
        this.sourceEncoding = sourceEncoding;
        this.sourceCharset = forName(sourceEncoding);
    }

    public String usemin(final String path, final String html) throws IOException {
        final Map<String, String> builds = new HashMap<>();

        final Matcher matcher = searchPattern.matcher(html);

        while (matcher.find()) {
            final String outputResourceName = matcher.group(1);
            logger.info("    {}{}{}", CYAN, outputResourceName, RESET);

            final boolean jspContextPath = startsWith(outputResourceName, JSP_CONTEXT_PATH);

            final Document resources = parse(matcher.group(2), sourceEncoding);
            final Optional<String> sourceMin = compile(path, resources);

            if (sourceMin.isPresent()) {
                final String outputResource = filerev(outputResourceName, sourceMin.get(), sourceCharset);

                final File outputFile = jspContextPath ?
                        new File(targetDirectory.getCanonicalFile(), substringAfter(outputResource, JSP_CONTEXT_PATH)) :
                        new File(new File(targetDirectory.getCanonicalFile(), path).getCanonicalFile(), outputResource);

                final File canonicalOutputFile = outputFile.getCanonicalFile();

                logger.info(EMPTY);
                logger.info("  {}Writing {}{}", GREEN, substringAfter(outputFile.getCanonicalPath(), targetDirectory.getCanonicalPath() + '/'), RESET);
                createParentDirs(canonicalOutputFile);
                touch(canonicalOutputFile);
                write(sourceMin.get(), canonicalOutputFile, sourceCharset);

                builds.put(outputResourceName, outputResource);
                logger.info(EMPTY);
            }
        }

        String outputHtml = html;
        if (!builds.isEmpty()) {

            for (final String fileName : builds.keySet()) {
                final String resourceName = builds.get(fileName);
                final String regex = searchPattern.pattern().replaceFirst(quote("(.*?)"), quoteReplacement(quote(fileName) + '?'));
                final Pattern pattern = Pattern.compile(regex, DOTALL);
                outputHtml = pattern.matcher(outputHtml).replaceFirst(quoteReplacement(getReplacement(resourceName)));
            }
        }

        return outputHtml;
    }

    protected abstract Optional<String> compile(final String path, final Document resources) throws IOException;

    protected abstract String getReplacement(final String resourceName);

}
