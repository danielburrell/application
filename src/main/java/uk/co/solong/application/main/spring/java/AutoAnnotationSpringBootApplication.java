package uk.co.solong.application.main.spring.java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.co.solong.application.annotations.RootConfiguration;
import uk.co.solong.application.main.spring.scanner.SpringClassFinder;

/**
 * Recommended class to use as a Spring Main.
 *
 * To be used in conjunction with RootConfiguration
 */
@SpringBootApplication
public class AutoAnnotationSpringBootApplication {

    public static void main(String[] args) throws ClassNotFoundException {
        SpringClassFinder finder = new SpringClassFinder();
        SpringApplication.run(new Class[] {AutoAnnotationSpringBootApplication.class, finder.findAnnotatedClasses(RootConfiguration.class)}, args);
    }
}
