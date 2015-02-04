package com.podbox.compiler;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.yahoo.platform.yui.compressor.CssCompressor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import static com.google.common.base.Optional.fromNullable;

public final class CssCompiler {

    private CssCompiler() {
    }

    public static Optional<String> compile(final List<String> sources) throws IOException {
        final String source = Joiner.on('\n').join(sources);
        final StringReader reader = new StringReader(source);

        final CssCompressor compressor = new CssCompressor(reader);

        final StringWriter writer = new StringWriter();
        try {
            compressor.compress(writer, -1);
        } finally {
            writer.close();
        }
        return fromNullable(writer.toString());
    }

}
