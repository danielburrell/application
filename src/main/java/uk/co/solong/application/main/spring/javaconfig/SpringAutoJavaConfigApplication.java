package uk.co.solong.application.main.spring.javaconfig;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import uk.co.solong.application.annotations.RootConfiguration;

/**
 * Generic application harness for Java-Config Annotation based applications
 * with one or more {@link RootConfiguration} annotations present.
 * <p>
 * Automatically searches for a Configuration class with
 * {@link RootConfiguration} annotation, and uses it as the root application
 * context.
 * </p>
 * If multiple classes are found, the (optional) first argument to the
 * application can be used to qualify by name.
 * 
 * @author Daniel Burrell
 *
 */
public class SpringAutoJavaConfigApplication {
    private static final Logger logger = LoggerFactory.getLogger(SpringAutoJavaConfigApplication.class);

    public void run(String qualifiedRootConfiguration) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        boolean started = false;
        try {
            logger.info("Scanning for root configuration");
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(RootConfiguration.class));
            Set<BeanDefinition> bd = scanner.findCandidateComponents("");

            if (bd.size() == 1) {
                logger.info("RootConfiguration found");
                String value = bd.iterator().next().getBeanClassName();
                started = startApp(context, value);
            } else if (bd.size() > 1) {
                logger.info("Multiple RootConfigurations found");
                if (StringUtils.isEmpty(qualifiedRootConfiguration)) {
                    throw new RuntimeException("Multiple RootConfigurations found, but no qualifier specified");
                }
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
                if (!found) {
                    throw new RuntimeException("Multiple RootConfigurations found, but none match the name: " + qualifiedRootConfiguration);
                }
            } else if (bd.size() == 0) {
                throw new RuntimeException("Must have at least 1 Configuration class annotated with @RootConfiguration on the classpath");
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
                    logger.info("Application has failed. Closing context failed too");
                }
            }
        }
    }

    private boolean startApp(AnnotationConfigApplicationContext context, String rootConfiguration) throws ClassNotFoundException {
        boolean started;
        logger.info("Using RootConfiguration: {}", rootConfiguration);
        ClassLoader classLoader = SpringAutoJavaConfigApplication.class.getClassLoader();
        Class<?> aClass = classLoader.loadClass(rootConfiguration);
        context.register(aClass);
        context.registerShutdownHook();
        context.refresh();
        logger.info("Application started");
        started = true;
        return started;
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            new SpringAutoJavaConfigApplication().run(args[0]);
        } else if (args.length < 1) {
            new SpringAutoJavaConfigApplication().run("");
        } else {
            throw new RuntimeException("Too many arguments. Expected either 1 RootConfiguration name, or nothing");
        }
    }

}