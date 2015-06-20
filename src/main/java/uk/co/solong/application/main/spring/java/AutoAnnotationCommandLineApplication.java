package uk.co.solong.application.main.spring.java;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import uk.co.solong.application.annotations.CommandLine;
import uk.co.solong.application.annotations.MainClass;
import uk.co.solong.application.annotations.RootConfiguration;
import asg.cliche.ShellFactory;

/**
 * <p>
 * Use this for command-line applications that require some JavaConfiguration to
 * be automatically registered.
 * </p>
 * <p>
 * Automatically searches for a Configuration class with
 * {@link RootConfiguration} annotation, and uses it as the root application
 * context.
 * </p>
 * <p>
 * Exactly 1 {@link CommandLine} annotated class may be present on the
 * classpath.
 * <p>
 * Multiple RootConfigurations candidates may be present on the classpath,
 * provided that the first arg to the application is the qualifier for the
 * {@link RootConfiguration} e.g:
 * </p>
 * <p>
 * <code>@RootConfiguration(name="someQualifier")</code>
 * </p>
 * 
 * @author Daniel Burrell
 *
 */
public class AutoAnnotationCommandLineApplication {
    private static final Logger logger = LoggerFactory.getLogger(AutoAnnotationCommandLineApplication.class);

    public void run(String rootConfigName) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        boolean started = false;
        try {
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(RootConfiguration.class));
            Set<BeanDefinition> bd = scanner.findCandidateComponents("");

            Validate.isTrue(bd.size() >= 1, "Must have at least 1 Configuration class annotated with @RootConfiguration on the classpath.");

            if (bd.size() == 1) {
                logger.info("RootConfiguration found");
                String value = bd.iterator().next().getBeanClassName();
                started = executeCommandLine(context, value);
            } else {
                logger.info("Multiple RootConfigurations found");
                Validate.isTrue(!StringUtils.isEmpty(rootConfigName), "Multiple RootConfigurations found, but no qualifier specified");
                boolean found = false;
                while (bd.iterator().hasNext()) {
                    String value = bd.iterator().next().getBeanClassName();
                    RootConfiguration r = Class.forName(value).getAnnotation(RootConfiguration.class);
                    if (rootConfigName.equals(r.name())) {
                        found = true;
                        started = executeCommandLine(context, value);
                        break;
                    }
                }

                Validate.isTrue(found, "Multiple RootConfigurations found, but none match the name: {}", rootConfigName);
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

    private boolean executeCommandLine(AnnotationConfigApplicationContext context, String rootContext) throws IOException, ClassNotFoundException {
        context.register(Class.forName(rootContext));
        context.refresh();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(CommandLine.class));
        Set<BeanDefinition> bd = scanner.findCandidateComponents("");
        Validate.isTrue(bd.size() == 1, "Expected exactly 1 class annotated with CommandLine to be present on the classpath, found {}", bd.size());

        logger.info("CommandLine class found");
        String value = bd.iterator().next().getBeanClassName();
        Class<?> aClass = Class.forName(value);
        CommandLine r = Class.forName(value).getAnnotation(CommandLine.class);
        Object cmdl = context.getBean(aClass);
        logger.info("Starting application");
        ShellFactory.createConsoleShell(r.prompt(), "", cmdl).commandLoop();

        context.close();
        return true;
    }

    public static void main(String[] args) {
        Validate.isTrue(args.length < 1, "Too many arguments. Expected either 1 RootConfiguration name, or nothing");
        if (args.length == 1) {
            new AutoAnnotationCommandLineApplication().run(args[0]);
        } else {
            new AutoAnnotationCommandLineApplication().run("");
        }
    }

}
