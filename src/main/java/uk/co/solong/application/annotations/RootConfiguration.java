package uk.co.solong.application.annotations;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import uk.co.solong.application.main.spring.java.AutoAnnotationApplication;
import uk.co.solong.application.main.spring.java.AutoAnnotationMethodApplication;
import uk.co.solong.application.main.spring.java.AutoAnnotationSpringBootApplication;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Place this annotation on the root JavaConfiguration class that should be registered with SpringBoot</p>
 * <p>For use with the following classes:</p>
 * <ol>
 * <li> {@link AutoAnnotationApplication} </li>
 * <li> {@link AutoAnnotationSpringBootApplication} </li>
 * <li> {@link AutoAnnotationMethodApplication} </li>
 * </ol>
 * @author Daniel Burrell
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface RootConfiguration {
    String name() default "";
}
