# CSS compression _(with LESS support)_
```html
<!-- build:css styles/app.css -->
<link rel="stylesheet" href="styles/main.css">
<link rel="stylesheet" href="styles/custom.css">
<link rel="stylesheet" type="text/x-less" href="styles/bootstrap.less"/>
<script src="libs/less-1.7.0.js"></script>
<!-- endbuild -->
```

# JS compilation
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

# JSP context path support
```html
<!-- build:css ${pageContext.request.contextPath}/styles/app.css -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/custom.css">
<!-- endbuild -->

<!-- build:js ${pageContext.request.contextPath}/scripts/podbox.js -->
<script src="${pageContext.request.contextPath}/scripts/app.js"></script>
<!-- endbuild -->
```

# CDN replace _(html files only)_
```html
<link rel="stylesheet" href="libs/jquery-ui-1.11.2.css"
      data-cdn="//cdnjs.cloudflare.com/ajax/libs/jqueryui/1.11.2/jquery-ui.min.css">

<script src="libs/jquery-ui-1.11.2.js"
        data-cdn="//cdnjs.cloudflare.com/ajax/libs/jqueryui/1.11.2/jquery-ui.min.js"></script>
```

# Maven Config
```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-war-plugin</artifactId>
            <configuration>
                <warSourceExcludes>
                    libs/*,
                    index.html,
                    mapping.html
                </warSourceExcludes>
            </configuration>
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
