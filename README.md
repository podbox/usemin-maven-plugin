# Usemin Maven Plugin
The Usemin Maven plugin is a tool inspired by [grunt-usemin](https://github.com/yeoman/grunt-usemin),
which replaces references from non-optimized scripts and stylesheets to their optimized version within a set of HTML and JSP files using :

* [Official LESS CSS Compiler for Java](https://github.com/marceloverdijk/lesscss-java)
* [YUI CSS Compressor](https://github.com/yui/yuicompressor)
* [Google Closure Compiler](https://github.com/google/closure-compiler)


## CSS compression _(with LESS support)_
```html
<!-- build:css styles/app.css -->
<link rel="stylesheet" href="styles/main.css">
<link rel="stylesheet" href="styles/custom.css">
<link rel="stylesheet" type="text/x-less" href="styles/bootstrap.less"/>
<script src="libs/less-1.7.0.js"></script>
<!-- endbuild -->
```

## JS compilation
```html
<!-- build:js scripts/podbox.js -->
<script>
    var toto = "podbox rocks !";
    console.log(toto);
</script>
<script src="scripts/app.js"></script>

<script src="scripts/controllers/mapping.js"></script>

<script src="scripts/directives/dragndrop.js"></script>

<script src="scripts/services/model.js"></script>
<script src="scripts/services/services.js"></script>
<!-- endbuild -->
```

## JSP context path support
```html
<!-- build:css ${pageContext.request.contextPath}/styles/app.css -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/custom.css">
<!-- endbuild -->

<!-- build:js ${pageContext.request.contextPath}/scripts/podbox.js -->
<script src="${pageContext.request.contextPath}/scripts/app.js"></script>
<!-- endbuild -->
```

## CDN replace _(html files only)_
```html
<link rel="stylesheet" href="libs/jquery-ui-1.11.2.css"
      data-cdn="//cdnjs.cloudflare.com/ajax/libs/jqueryui/1.11.2/jquery-ui.min.css">

<script src="libs/jquery-ui-1.11.2.js"
        data-cdn="//cdnjs.cloudflare.com/ajax/libs/jqueryui/1.11.2/jquery-ui.min.js"></script>
```

## Maven Config
```xml
<pluginRepositories>
    <pluginRepository>
        <id>podbox-public-repository</id>
        <url>http://ci.podbox.com/nexus/content/repositories/public</url>
    </pluginRepository>
</pluginRepositories>

<build>
    <plugins>
        <plugin>
            <artifactId>maven-war-plugin</artifactId>
            <executions>
                <execution>
                    <id>default-war</id>
                    <phase>package</phase>
                    <goals>
                        <goal>war</goal>
                    </goals>
                    <configuration>
                        <warSourceExcludes>
                            index.html,
                            mapping.html
                        </warSourceExcludes>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        <plugin>
            <groupId>com.podbox</groupId>
            <artifactId>usemin-maven-plugin</artifactId>
            <executions>
                <execution>
                    <phase>prepare-package</phase>
                    <goals>
                        <goal>usemin</goal>
                    </goals>
                    <configuration>
                        <!-- http://javadoc.closure-compiler.googlecode.com/git/com/google/javascript/jscomp/CompilerOptions.LanguageMode.html -->
                        <!-- default: ECMASCRIPT5_STRICT -->
                        <languageMode>ECMASCRIPT5_STRICT</languageMode>
                        
                        <!-- http://javadoc.closure-compiler.googlecode.com/git/com/google/javascript/jscomp/CompilationLevel.html -->
                        <!-- default: SIMPLE_OPTIMIZATIONS -->
                        <compilationLevel>ADVANCED_OPTIMIZATIONS</compilationLevel>
                        
                        <!-- https://github.com/marceloverdijk/lesscss-java -->
                        <!-- default: no options -->
                        <lessOptions>
                            <lessOption>--relative-urls</lessOption>
                        </lessOptions>
                        
                        <sources>
                            <source>index.html</source>
                            <source>mapping.html</source>
                        </sources>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Gradle Task _(yeah, it's ugly...)_
```groovy
buildscript {
    repositories {
        mavenCentral()
        maven { url 'http://ci.podbox.com/nexus/content/repositories/public' }
    }
    dependencies {
        classpath "com.podbox:usemin-maven-plugin:$useminPluginVersion"
    }
}

sourceSets { main { resources { exclude 'static/index.html' } } }

task usemin(dependsOn: processResources) {
    def usemin = new com.podbox.UseMin(
            sourceEncoding:   'UTF-8',
            sourceDirectory:  file('src/main/resources/static'),
            targetDirectory:  file('build/resources/main/static'),
            languageMode:     'ECMASCRIPT5_STRICT',
            compilationLevel: 'SIMPLE_OPTIMIZATIONS',
            sources:          [ 'index.html' ]
    )
    usemin.execute()
}
```
