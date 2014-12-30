package com.podbox.compiler;

import com.google.common.base.Optional;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import static com.google.common.base.Optional.fromNullable;
import static java.lang.System.setOut;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

public class LessCssCompiler {

    private static final PrintStream LESS_OUT = new LessPrintStream(System.out);

    private static final PrintStream SYSTEM_OUT = System.out;

    private static final Logger LOGGER = getLogger(LessCssCompiler.class);

    public static Optional<String> compile(final List<File> sources) throws IOException {
        setOut(LESS_OUT);

        try {
            final LessCompiler lessCompiler = new LessCompiler();

            String sourceMin = "";
            for (final File sourceFile : sources) {
                try {
                    sourceMin += lessCompiler.compile(sourceFile);
                } catch (final LessException e) {
                    throw new IOException(e.getMessage(), e);
                }
            }

            return fromNullable(trimToNull(sourceMin));

        } finally {
            setOut(SYSTEM_OUT);
        }
    }

    public static class LessPrintStream extends PrintStream {

        public LessPrintStream(final OutputStream out) {
            super(out);
        }

        @Override
        public void println(final String x) {
            if (!"done".equals(x)) {
                LOGGER.info(x);
            }
        }

    }

}
