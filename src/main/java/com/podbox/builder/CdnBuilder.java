package com.podbox.builder;

import com.google.common.base.Optional;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;

import static com.podbox.ansi.AnsiColor.CYAN;
import static com.podbox.ansi.AnsiColor.RESET;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.jsoup.Jsoup.parse;

public class CdnBuilder extends AbstractBuilder {

    public CdnBuilder(final File sourceDirectory, final File targetDirectory, final String sourceEncoding) {
        super(null, sourceDirectory, targetDirectory, sourceEncoding);
    }

    @Override
    public String usemin(final String path, final String html) throws IOException {
        final Document document = parse(html, sourceEncoding);

        boolean hasCdnData = false;

        for (final Element element : document.select("link[data-cdn]")) {
            final String cdn = element.attr("data-cdn");
            logger.info("    {}{}{}  ⟶   {}{}{}", CYAN, element.attr("href"), RESET, CYAN, cdn, RESET);
            element.attr("href", cdn).removeAttr("data-cdn");
            hasCdnData = true;
        }

        for (final Element element : document.select("script[data-cdn]")) {
            final String cdn = element.attr("data-cdn");
            logger.info("    {}{}{}  ⟶   {}{}{}", CYAN, element.attr("src"), RESET, CYAN, cdn, RESET);
            element.attr("src", cdn).removeAttr("data-cdn");
            hasCdnData = true;
        }

        if (hasCdnData) {
            logger.info(EMPTY);
            return document.outerHtml();
        }
        else {
            return html;
        }
    }

    @Override
    protected Optional<String> compile(final String path, final Document resources) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String getReplacement(final String resourceName) {
        throw new UnsupportedOperationException();
    }

}
