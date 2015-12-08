package com.podbox.builder;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Optional.absent;
import static com.google.common.io.Files.copy;
import static com.google.common.io.Files.createParentDirs;
import static com.podbox.ansi.AnsiColor.CYAN;
import static com.podbox.ansi.AnsiColor.RESET;
import static com.podbox.compiler.FileRevCompiler.filerev;
import static java.util.Arrays.asList;
import static java.util.regex.Pattern.DOTALL;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.filefilter.CanReadFileFilter.CAN_READ;
import static org.apache.commons.lang3.StringUtils.*;

public class FileRevBuilder extends AbstractBuilder {

    static final Pattern SEARCH_PATTERN = Pattern.compile("<!--[\\s]*build:filerev[\\s]*(.*?)(?=[\\s]*-->)[\\s]*-->(.*?)(?=<!--[\\s]*endbuild[\\s]*-->)<!--[\\s]*endbuild[\\s]*-->", DOTALL);

    public FileRevBuilder(final File sourceDirectory, final File targetDirectory, final String sourceEncoding, String fileRevOption) {
        super(SEARCH_PATTERN, sourceDirectory, targetDirectory, sourceEncoding, fileRevOption);
    }

    @Override
    protected Optional<String> compile(final String path, final Document resources) throws IOException {
        final File inputDirectory = new File(sourceDirectory, path);
        final File outputDirectory = new File(targetDirectory, path);

        final Element script = resources.select("script").first();
        if (script != null) {
            final String functionName = script.attr("data-function");
            final String namePrefix = script.attr("data-namePrefix");
            final String nameSuffix = script.attr("data-nameSuffix");

            if (isNotBlank(functionName) && isNotBlank(namePrefix) && isNotBlank(nameSuffix)) {

                final Map<String, String> dictionnary = new HashMap<>();

                final Iterator<File> inputFiles = listFiles(new File(inputDirectory, namePrefix), new SuffixFileFilter(nameSuffix), CAN_READ).iterator();
                while (inputFiles.hasNext()) {
                    final File inputFile = inputFiles.next();
                    final File canonicalInputFile = inputFile.getCanonicalFile();

                    final String outputFileName = filerev(inputFile, sourceCharset);
                    final String outputPath = substringBefore(substringAfter(substringAfter(inputFile.getAbsolutePath(), inputDirectory.getAbsolutePath()), namePrefix), inputFile.getName());

                    final File outputFile = new File(new File(new File(outputDirectory, namePrefix), outputPath), outputFileName);
                    final File canonicalOutputFile = outputFile.getCanonicalFile();

                    createParentDirs(canonicalOutputFile);
                    copy(canonicalInputFile, canonicalOutputFile);

                    final String inputResourceName = substringBeforeLast(substringAfter(substringAfter(inputFile.getAbsolutePath(), inputDirectory.getAbsolutePath()), namePrefix), nameSuffix);
                    final String outputResourceName = substringBeforeLast(substringAfter(substringAfter(outputFile.getAbsolutePath(), outputDirectory.getAbsolutePath()), namePrefix), nameSuffix);
                    logger.info("    {}─ {}{}{}  ⟶   {}{}{}", inputFiles.hasNext() ? '├' : '└', CYAN, inputResourceName, RESET, CYAN, outputResourceName, RESET);

                    dictionnary.put('"' + inputResourceName + '"', '"' + outputResourceName + '"');
                }

                return Optional.of(Joiner.on(' ').join(asList(
                        "'use strict';(function(w){var _hash={",
                        Joiner.on(',').withKeyValueSeparator(":").join(dictionnary),
                        "};w." + functionName + "=function(name){",
                        "return \"" + namePrefix + "\" + _hash[name] + \"" + nameSuffix + "\";}})(window)"
                )));
            }
        }

        return absent();
    }

    @Override
    protected String getReplacement(final String resourceName) {
        return "<script src=\"" + resourceName + "\"></script>";
    }

}
