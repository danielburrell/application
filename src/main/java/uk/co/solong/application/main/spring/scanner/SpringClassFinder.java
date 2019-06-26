package uk.co.solong.application.main.spring.scanner;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;

import java.lang.annotation.Annotation;

public class SpringClassFinder {

    public Class<?> findAnnotatedClasses(Class<? extends Annotation> annotationType) throws ClassNotFoundException {
        return findAnnotatedClasses(annotationType, "");
    }

    public Class<?> findAnnotatedClasses(Class<? extends Annotation> annotationType, String whitelistPackages) throws ClassNotFoundException {

        try (ScanResult scanResult = extractGraph(whitelistPackages)) {                   // Start the scan
            for (ClassInfo routeClassInfo : scanResult.getClassesWithAnnotation(annotationType.getCanonicalName())) {
                return Class.forName(routeClassInfo.getName());
            }
        }
        throw new RuntimeException("Could not find any class annotated with @RootConfiguration");
    }

    private ScanResult extractGraph(String whitelistPackages) {
        ClassGraph classGraph = new ClassGraph();
        classGraph.enableAllInfo();
        if (!StringUtils.isBlank(whitelistPackages)) {
            classGraph.whitelistPackages(whitelistPackages);
        }
        return classGraph.blacklistPackages("org.springframework", "org.jboss")
        .scan();

    }
}
