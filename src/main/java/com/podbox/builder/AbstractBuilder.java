package com.podbox.builder;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import org.apache.maven.plugin.logging.Log;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.hash.Hashing.murmur3_32;
import static com.google.common.io.Files.*;
import static java.nio.charset.Charset.forName;
import static java.util.regex.Matcher.quoteReplacement;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.quote;
import static org.apache.commons.lang3.StringUtils.*;
import static org.jsoup.Jsoup.parse;

public abstract class AbstractBuilder {

    protected static final String JSP_CONTEXT_PATH = "${pageContext.request.contextPath}/";

    protected final Pattern searchPattern;

    protected final Log log;

    protected final File sourceDirectory;

    private final File targetDirectory;

    protected final String sourceEncoding;

    protected final Charset sourceCharset;

    protected AbstractBuilder(final Pattern searchPattern, final Log log, final File sourceDirectory, final File targetDirectory, final String sourceEncoding) {
        this.searchPattern = searchPattern;
        this.log = log;
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
            log.info("    " + outputResourceName);

            final boolean jspContextPath = startsWith(outputResourceName, JSP_CONTEXT_PATH);

            final Document resources = parse(matcher.group(2), sourceEncoding);
            final Optional<String> sourceMin = compile(path, resources);

            if (sourceMin.isPresent()) {
                final String hash = murmur3_32().hashString(sourceMin.get(), UTF_8).toString();

                final String outputResource = Joiner.on('.').join(
                        substringBeforeLast(outputResourceName, "."),
                        hash,
                        substringAfterLast(outputResourceName, ".")
                );
                
                final File outputFile = jspContextPath ?
                        new File(targetDirectory.getCanonicalFile(), substringAfter(outputResource, JSP_CONTEXT_PATH)) :
                        new File(new File(targetDirectory.getCanonicalFile(), path).getCanonicalFile(), outputResource);

                log.info("");
                log.info("  Writing " + outputFile.getCanonicalFile().getAbsolutePath());
                createParentDirs(outputFile);
                touch(outputFile.getCanonicalFile());
                write(sourceMin.get(), outputFile.getCanonicalFile(), sourceCharset);

                builds.put(outputResourceName, outputResource);
                log.info("");
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
