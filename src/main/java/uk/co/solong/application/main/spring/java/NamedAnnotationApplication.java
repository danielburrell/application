package uk.co.solong.application.main.spring.java;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * <p>
 * Use this for background applications that require some JavaConfiguration to
 * be manually registered by specifying the fully qualified classname as the
 * first argument to the application.
 * </p>
 * <p>
 * Attempts to load the (mandatory) first arg as a class and register it with
 * the application context.
 * </p>
 * <p>
 * Only use this class if you have a requirement to explicitly provide the root
 * config class as a parameter, otherwise use {@link AutoAnnotationApplication}
 * instead.
 * </p>
 * 
 * @author Daniel Burrell
 *
 */
public class NamedAnnotationApplication {
    private static final Logger logger = LoggerFactory.getLogger(NamedAnnotationApplication.class);

    public void run(String configClass) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        boolean started = false;
        try {
            ClassLoader classLoader = NamedAnnotationApplication.class.getClassLoader();
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
                    logger.error("Application has failed. Closing context failed too");
                }
            }
        }
    }

    public static void main(String[] args) {
        Validate.isTrue(args.length == 1, "Expected fully-qualified configuration classname as only parameter.");
        new NamedAnnotationApplication().run(args[0]);
    }

}
