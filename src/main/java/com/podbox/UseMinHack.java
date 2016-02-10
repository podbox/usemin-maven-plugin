package com.podbox;

import org.springframework.boot.loader.tools.JarWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;

public final class UseMinHack {
    private UseMinHack() {
    }

    public static void run(final File sourceFile, final File tmpFile, final File useminOutputDirectory, final List<String> entries) throws IOException {

        try (final JarFile sourceJarFile = new JarFile(sourceFile)) {
            final JarWriter writer = new JarWriter(tmpFile);
            try {
                writer.writeEntries(sourceJarFile);

                for (final String entryName : entries) {
                    final File entryFile = new File(useminOutputDirectory, entryName);

                    try (final FileInputStream input = new FileInputStream(entryFile)) {
                        writer.writeEntry(entryName, input);
                    }
                }
            }
            finally {
                writer.close();
            }

        }
        sourceFile.delete();
        tmpFile.renameTo(sourceFile);
    }
}
