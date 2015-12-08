package com.podbox.compiler;

import com.google.common.base.Joiner;
import com.google.common.hash.HashFunction;
import com.google.common.io.Files;
import com.podbox.builder.FileRevOption;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static com.google.common.hash.Hashing.murmur3_32;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

public final class FileRevCompiler {

    private static final HashFunction HASH_FUNCTION = murmur3_32();

    private FileRevCompiler() {
    }

    public static String hash(final File input, final Charset charset) throws IOException {
        return HASH_FUNCTION.hashString(Files.toString(input, charset), charset).toString();
    }

    public static String hash(final String input, final Charset charset) {
        return HASH_FUNCTION.hashString(input, charset).toString();
    }

    public static String filerev(final String originalFileName, final String input, final Charset charset) {
        return Joiner.on('.').join(
                substringBeforeLast(originalFileName, "."),
                hash(input, charset),
                substringAfterLast(originalFileName, ".")
        );
    }

    public static String filerev(final File originalFile, final Charset charset) throws IOException {
        final String originalFileName = originalFile.getName();

        return Joiner.on('.').join(
                substringBeforeLast(originalFileName, "."),
                hash(originalFile, charset),
                substringAfterLast(originalFileName, ".")
        );
    }

    public static String filerev(final String originalFileName, final String input, final Charset charset, String fileRevOption) {
        if(fileRevOption.equals(FileRevOption.AS_PARAMETER)) {
            return originalFileName;
        } else {
            return filerev(originalFileName, input, charset);
        }
    }

}
