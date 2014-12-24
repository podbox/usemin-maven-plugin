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
import static java.util.regex.Pattern.DOTALL;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;
import static org.jsoup.Jsoup.parse;

public abstract class AbstractBuilder {

    protected final Pattern searchPattern;

    protected final Log log;

    protected final String sourceEncoding;

    protected final Charset sourceCharset;

    protected AbstractBuilder(final Pattern searchPattern, final Log log, final String sourceEncoding) {
        this.searchPattern = searchPattern;
        this.log = log;
        this.sourceEncoding = sourceEncoding;
        this.sourceCharset = forName(sourceEncoding);
    }

    public String usemin(final File sourceDirectory, final File targetDirectory, final String html) throws IOException {
        final Map<String, String> builds = new HashMap<>();

        final Matcher matcher = searchPattern.matcher(html);

        while (matcher.find()) {
            final String outputFileName = matcher.group(1);
            log.info("    " + outputFileName);

            final Document resources = parse(matcher.group(2), sourceEncoding);
            final Optional<String> sourceMin = compile(resources, sourceDirectory, targetDirectory);

            if (sourceMin.isPresent()) {
                final String hash = murmur3_32().hashString(sourceMin.get(), UTF_8).toString();

                final String outputResource = Joiner.on('.').join(
                        substringBeforeLast(outputFileName, "."),
                        hash,
                        substringAfterLast(outputFileName, ".")
                );

                final File outputFile = new File(targetDirectory, outputResource);
                createParentDirs(outputFile);
                touch(outputFile.getCanonicalFile());
                write(sourceMin.get(), outputFile.getCanonicalFile(), sourceCharset);

                builds.put(outputFileName, outputResource);
                log.info("");
            }
        }

        String outputHtml = html;
        if (!builds.isEmpty()) {

            for (final String fileName : builds.keySet()) {
                final String resourceName = builds.get(fileName);
                outputHtml = Pattern.compile(searchPattern.pattern()
                        .replaceFirst("\\(\\.\\*\\?\\)", escapeJava(fileName) + '?'), DOTALL).matcher(outputHtml)
                        .replaceFirst(getReplacement(resourceName));
            }
        }

        return outputHtml;
    }

    protected abstract Optional<String> compile(final Document resources, final File sourceDirectory, final File targetDirectory) throws IOException;

    protected abstract String getReplacement(final String resourceName);

}
