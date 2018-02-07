package uk.co.solong.application.main.spring.java;

import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import uk.co.solong.application.annotations.RootConfiguration;

/**
 * <p>
 * Use this for background applications that require some JavaConfiguration to
 * be automatically registered.
 * </p>
 * <p>
 * Automatically searches for a Configuration class with
 * {@link RootConfiguration} annotation, and registers it with the application
 * context.
 * </p>
 * <p>
 * Multiple RootConfigurations candidates may be present on the classpath,
 * provided that the first arg to the application is the qualifier for the
 * {@link RootConfiguration} e.g: </p>
 * <p>
 * <code>@RootConfiguration(name="someQualifier")</code>
 * </p>
 * 
 * @author Daniel Burrell
 *
 */
public class AutoAnnotationApplication {
    private static final Logger logger = LoggerFactory.getLogger(AutoAnnotationApplication.class);

    public void run(String qualifiedRootConfiguration) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        boolean started = false;
        try {
            logger.info("Scanning for root configuration");
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(RootConfiguration.class));
            Set<BeanDefinition> bd = scanner.findCandidateComponents("");

            Validate.isTrue(bd.size() >= 1, "Must have at least 1 Configuration class annotated with @RootConfiguration on the classpath");

            if (bd.size() == 1) {
                logger.info("RootConfiguration found");
                String value = bd.iterator().next().getBeanClassName();
                started = startApp(context, value);
            } else {
                logger.info("Multiple RootConfigurations found");
                Validate.isTrue(!StringUtils.isEmpty(qualifiedRootConfiguration), "Multiple RootConfigurations found, but no qualifier specified");
                boolean found = false;
                while (bd.iterator().hasNext()) {
                    String value = bd.iterator().next().getBeanClassName();
                    RootConfiguration r = Class.forName(value).getAnnotation(RootConfiguration.class);
                    if (qualifiedRootConfiguration.equals(r.name())) {
                        found = true;
                        started = startApp(context, value);
                        break;
                    }
                }
                Validate.isTrue(found, "Multiple RootConfigurations found, but none match the name: {}", qualifiedRootConfiguration);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Application failed to start", e);
        } catch (Exception e) {
            throw new RuntimeException("Application failed to start", e);
        } finally {
            if (!started) {
                try {
                    context.close();
                } catch (Throwable e) {
                    logger.error("Application has failed. Closing context failed too");
                }
            }
        }
    }

    private boolean startApp(AnnotationConfigApplicationContext context, String rootConfiguration) throws ClassNotFoundException {
        boolean started;
        logger.info("Using RootConfiguration: {}", rootConfiguration);
        ClassLoader classLoader = AutoAnnotationApplication.class.getClassLoader();
        Class<?> aClass = classLoader.loadClass(rootConfiguration);
        context.register(aClass);
        context.registerShutdownHook();
        context.refresh();
        logger.info("Application started");
        started = true;
        return started;
    }

    public static void main(String[] args) {
        Validate.isTrue(args.length <= 1, "Too many arguments. Expected either 1 RootConfiguration name, or nothing");
        if (args.length == 1) {
            new AutoAnnotationApplication().run(args[0]);
        } else {
            new AutoAnnotationApplication().run("");
        }
    }

}
