package uk.co.solong.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Generic application harness for Java-Config Annotation based applications. To
 * use, pass the top-level config class as the first (and only) parameter.
 * 
 * @author Daniel Burrell
 *
 */
public class SpringAnnotationContextApplication {
    private static final Logger logger = LoggerFactory.getLogger(SpringAnnotationContextApplication.class);

    public void run(String configClass) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        boolean started = false;
        try {
            ClassLoader classLoader = SpringAnnotationContextApplication.class.getClassLoader();
            Class<?> aClass = classLoader.loadClass(configClass);
            context.register(aClass);
            context.registerShutdownHook();
            context.refresh();
            logger.info("Application started");
            started = true;
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

    public static void main(String[] args) {
        new SpringAnnotationContextApplication().run(args[0]);
    }

}
