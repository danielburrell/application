Application is a library which provides a set of standard application and configuration classes based on common usage patterns and in particular for use with the spring framework.

# Quickstart

## Maven
```xml
<dependency>
    <groupId>uk.co.solong</groupId>
    <artifactId>application</artifactId>
    <version>0.0.18</version>
</dependency>
```

## AutoAnnotationSpringBootApplication
We recommend you use the AutoAnnotationSpringBootApplication class to enable you to start a SpringBoot application by automatically searching for a @RootConfiguration annotated class.

#### Single RootConfiguration Example
Create your standard SpringConfig class and add @RootConfiguration in addition to the regular spring @Configuration annotation.
```java
@Import({ PropertyPlaceholderConfig.class, ... })
@Configuration
@RootConfiguration
public class Config {

}
```

Then when you use the maven-assembly-plugin, simply provide AutoAnnotationApplication as the main class. No need to pass the name of the RootConfiguration class.
This is useful since the pom file does not need refactoring should the package change or root configuration class be renamed.

```xml
<programs>
   <program>
      <mainClass>uk.co.solong.application.main.spring.java.AutoAnnotationSpringBootApplication</mainClass>
      <id>start</id>
      <jvmSettings>
         <initialMemorySize>20m</initialMemorySize>
         <maxMemorySize>256m</maxMemorySize>
         <maxStackSize>128m</maxStackSize>
         <systemProperties>
            <systemProperty>logback.configurationFile=logback-prod.xml</systemProperty>
            <systemProperty>APP_ENV=prod</systemProperty>
         </systemProperties>
      </jvmSettings>
   </program>
</programs>
```

That's it! Nothing more to do!

#### Advanced examples
More advanced examples (including handling multiple RootConfiguration classes in the same library/classpath can be found on [this advanced example page](Advanced Examples)

---

## NamedAnnotationApplication
The NamedAnnotationApplication starts an application when given the fully qualified name of the Root configuration class as an explicit arg parameter. The class must exist, or the application will fail.

In the example below, the class com.company.config.Config is explicitly passed as a commandline argument in the maven assembler plugin.

```java
package com.company.config;

@Import({ PropertyPlaceholderConfig.class, ... })
@Configuration
public class Config {

}
```

```xml
<programs>
    <program>
        <mainClass>uk.co.solong.application.main.spring.java.NamedAnnotationApplication</mainClass>
        <id>start</id>
        <commandLineArguments>
            <commandLineArgument>com.company.config.Config</commandLineArgument>
        </commandLineArguments>
        <jvmSettings>
            <initialMemorySize>20m</initialMemorySize>
            <maxMemorySize>256m</maxMemorySize>
            <maxStackSize>128m</maxStackSize>
            <systemProperties>
                <systemProperty>logback.configurationFile=logback-prod.xml</systemProperty>
                <systemProperty>APP_ENV=prod</systemProperty>
            </systemProperties>
        </jvmSettings>
    </program>
</programs>
```

---

# Developer Guide
The [developer guide](https://github.com/danielburrell/developer-environment/wiki) explains how to setup the developer environment.
The [sdlc guide](https://github.com/danielburrell/sdlc/wiki/perform-release) page describes how to cut a release, publish artifacts to the central repository or github.