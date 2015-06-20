package uk.co.solong.application.main.spring.javaconfig;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

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
 * Generic application harness for Java-Config Annotation based applications. To
 * use, pass the top-level config class as the first (and only) parameter.
 * //INCOMPLETE - just a copy paste. doesn't actually scan for a method yet.
 * make it the second parameter if you have to be explicit
 * 
 * @author Daniel Burrell
 *
 */
public class SpringAutoJavaConfigMethodApplication {
    private static final Logger logger = LoggerFactory.getLogger(SpringAutoJavaConfigMethodApplication.class);

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
        boolean started = false;
        logger.info("Using RootConfiguration: {}", rootConfiguration);
        ClassLoader classLoader = SpringAutoJavaConfigMethodApplication.class.getClassLoader();
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
            if (methods.size() == 1) {
                Object d = context.getBean(clazz);
                try {
                    Method m = methods.iterator().next();
                    if (m.getParameterCount() > 1) {
                        throw new RuntimeException("Cannot call a non zero argument main method");
                    } else {
                        started = true;
                        m.invoke(d);
                    }
                    
                } catch (Throwable e){
                    throw new RuntimeException("Could not invoke zero argument method");                    
                }
            } else if (methods.size() == 0) {
                throw new RuntimeException("No MainMethod annotation found in MainClass");
            } else {
                throw new RuntimeException("Multiple MainMethod annotations found in MainClass. Expected exactly 1.");
            }
            
        }
        return started;
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            new SpringAutoJavaConfigMethodApplication().run(args[0]);
        } else if (args.length < 1) {
            new SpringAutoJavaConfigMethodApplication().run("");
        } else {
            throw new RuntimeException("Too many arguments. Expected either 1 RootConfiguration name, or nothing");
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
