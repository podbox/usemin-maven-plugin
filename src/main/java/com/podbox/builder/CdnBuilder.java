package com.podbox.builder;

import com.google.common.base.Optional;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;

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
            logger.info("  " + element.attr("href") + "  ⟶  " + element.attr("data-cdn"));
            element.attr("href", element.attr("data-cdn")).removeAttr("data-cdn");
            hasCdnData = true;
        }

        for (final Element element : document.select("script[data-cdn]")) {
            logger.info("  " + element.attr("src") + "  ⟶  " + element.attr("data-cdn"));
            element.attr("src", element.attr("data-cdn")).removeAttr("data-cdn");
            hasCdnData = true;
        }

        if (hasCdnData) {
            logger.info("");
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
