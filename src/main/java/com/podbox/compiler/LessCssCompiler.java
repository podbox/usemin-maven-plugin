package com.podbox.compiler;

import com.google.common.base.Optional;
import org.apache.maven.plugin.logging.Log;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Optional.fromNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

public class LessCssCompiler {

    public static Optional<String> compile(final Log log, final List<File> sources) throws IOException {
        final LessCompiler lessCompiler = new LessCompiler();

        String sourceMin = "";
        for (final File sourceFile : sources) {
            try {
                sourceMin += lessCompiler.compile(sourceFile);
            } catch (final LessException e) {
                log.error(e.getMessage());
                throw new IOException(e.getMessage(), e);
            }
        }

        return fromNullable(trimToNull(sourceMin));
    }

}
