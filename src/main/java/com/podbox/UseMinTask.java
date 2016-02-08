package com.podbox;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public class UseMinTask extends DefaultTask {

    @Input
    public String sourceEncoding = "UTF-8";

    @Input
    public File sourceDirectory;

    @Input
    public File targetDirectory;

    @Input
    public String languageMode = "ECMASCRIPT5_STRICT";

    @Input
    public String compilationLevel = "SIMPLE_OPTIMIZATIONS";

    @Input
    public String[] sources;

    @TaskAction
    public void run() throws IOException {
        final UseMin usemin = new com.podbox.UseMin();
        usemin.setSourceEncoding(sourceEncoding);
        usemin.setSourceDirectory(sourceDirectory);
        usemin.setTargetDirectory(targetDirectory);
        usemin.setLanguageMode(languageMode);
        usemin.setCompilationLevel(compilationLevel);
        usemin.setSources(sources);
        usemin.execute();
    }
}
