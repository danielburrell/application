package uk.co.solong.application.main;

import java.util.List;
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
 * Generic application harness for Java-Config Annotation based applications. To
 * use, pass the top-level config class as the first (and only) parameter.
 * 
 * @author Daniel Burrell
 *
 */
public class SpringAutoAnnotationContextApplication {
    private static final Logger logger = LoggerFactory.getLogger(SpringAutoAnnotationContextApplication.class);

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
                while (bd.iterator().hasNext()){
                    String value = bd.iterator().next().getBeanClassName();
                    RootConfiguration r = Class.forName(value).getAnnotation(RootConfiguration.class);
                    if (qualifiedRootConfiguration.equals(r.name())){
                        found = true;
                        started = startApp(context, value);
                        break;
                    }
                }
                if (!found) {
                    throw new RuntimeException("Multiple RootConfigurations found, but none match the name: "+qualifiedRootConfiguration);
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

    private boolean startApp(AnnotationConfigApplicationContext context, String value) throws ClassNotFoundException {
        boolean started;
        ClassLoader classLoader = SpringAutoAnnotationContextApplication.class.getClassLoader();
        Class<?> aClass = classLoader.loadClass(value);
        context.register(aClass);
        context.registerShutdownHook();
        context.refresh();
        logger.info("Application started");
        started = true;
        return started;
    }

    public static void main(String[] args) {
        new SpringAutoAnnotationContextApplication().run(args[0]);
    }

}
