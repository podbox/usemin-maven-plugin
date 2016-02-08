package com.podbox.compiler;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.css.SourceCode;
import com.google.common.css.compiler.ast.GssParser;
import com.google.common.css.compiler.ast.GssParserException;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Optional.of;
import static com.google.common.css.compiler.passes.CompactPrinter.printCompactly;
import static org.slf4j.LoggerFactory.getLogger;

public final class CssCompiler {

    static final Logger LOGGER = getLogger(CssCompiler.class);

    private CssCompiler() {
    }

    public static Optional<String> compile(final List<String> sources) throws IOException {
        final List<SourceCode> sourceCodes = new ArrayList<>();
        for (String source : sources) {
            sourceCodes.add(new SourceCode(null, source));
        }

        final GssParser gssParser = new GssParser(sourceCodes);
        try {
            return of(printCompactly(gssParser.parse()));
        }
        catch (final GssParserException e) {
            LOGGER.warn(e.getMessage());
        }

        // fallback
        return of(Joiner.on('\n').join(sources));
    }
}
