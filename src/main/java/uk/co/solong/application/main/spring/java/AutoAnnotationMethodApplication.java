package uk.co.solong.application.main.spring.java;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import uk.co.solong.application.annotations.MainClass;
import uk.co.solong.application.annotations.MainMethod;
import uk.co.solong.application.annotations.RootConfiguration;

/**
 * <p>
 * Use this class when you have an application that needs to be started by
 * invoking some non static, zero-arg method on some class, together with some
 * JavaConfig that needs to be registered with an ApplicationContext
 * </p>
 * <p>
 * Creates an AnnotationConfigApplicationContext, scans the classpath for a
 * class marked as {@link RootConfiguration} and registers this with the
 * context. Searches the context for a class annotated as {@link MainClass} and
 * invokes the method annotated with {@link MainMethod} thereby starting the
 * application.
 * </p>
 * 
 * <p>
 * Exactly 1 {@link MainClass} annotated class may be present on the classpath.
 * <p>
 * Multiple RootConfigurations candidates may be present on the classpath,
 * provided that the first arg to the application is the qualifier for the
 * {@link RootConfiguration} e.g:
 * </p>
 * <p>
 * <code>@RootConfiguration(name="someQualifier")</code>
 * </p>
 * 
 * 
 * @author Daniel Burrell
 *
 */
public class AutoAnnotationMethodApplication {
    private static final Logger logger = LoggerFactory.getLogger(AutoAnnotationMethodApplication.class);

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
                    logger.error("Application has failed. Closing context failed too");
                }
            }
        }
    }

    private boolean startApp(AnnotationConfigApplicationContext context, String rootConfiguration) throws ClassNotFoundException {
        boolean started = false;
        logger.info("Using RootConfiguration: {}", rootConfiguration);
        ClassLoader classLoader = AutoAnnotationMethodApplication.class.getClassLoader();
        Class<?> aClass = classLoader.loadClass(rootConfiguration);
        context.register(aClass);
        context.registerShutdownHook();
        context.refresh();
        logger.info("Configuration complete");
        logger.info("Searching for main class");
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(MainClass.class));
        Set<BeanDefinition> bd = scanner.findCandidateComponents("");
        if (bd.size() == 1) {
            logger.info("RootConfiguration found");
            String value = bd.iterator().next().getBeanClassName();
            Class<?> clazz = Class.forName(value);
            Collection<Method> methods = methodWithAnnotation(clazz, MainMethod.class);
            Validate.isTrue(methods.size() == 1, "Expected exactly 1 MainMethod, found {}", methods.size());

            Object d = context.getBean(clazz);

            Method m = methods.iterator().next();
            Validate.isTrue(m.getParameterCount() == 0, "Expected 0-arg MainMethod, but found {} arguments", m.getParameterCount());
            started = true;
            try {
                m.invoke(d);
            } catch (Throwable e) {
                throw new RuntimeException("Could not invoke zero argument method");
            }

        }
        return started;
    }

    public static void main(String[] args) {
        Validate.isTrue(args.length < 1, "Too many arguments. Expected either 1 RootConfiguration name, or nothing");
        if (args.length == 1) {
            new AutoAnnotationMethodApplication().run(args[0]);
        } else {
            new AutoAnnotationMethodApplication().run("");
        }
    }

    public static Collection<Method> methodWithAnnotation(Class<?> classType, Class<? extends Annotation> annotationClass) {

        if (classType == null)
            throw new NullPointerException("classType must not be null");

        if (annotationClass == null)
            throw new NullPointerException("annotationClass must not be null");

        Collection<Method> result = new ArrayList<Method>();
        for (Method method : classType.getMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
                result.add(method);
            }
        }
        return result;
    }

}
