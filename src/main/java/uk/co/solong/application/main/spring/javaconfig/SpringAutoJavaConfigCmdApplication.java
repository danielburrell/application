package uk.co.solong.application.main.spring.javaconfig;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import uk.co.solong.application.annotations.CommandLine;
import uk.co.solong.application.annotations.RootConfiguration;
import asg.cliche.ShellFactory;

/**
 * Generic application harness for Java-Config Annotation based command line
 * applications with one or more {@link RootConfiguration} annotations present.
 * <p>
 * Automatically searches for a Configuration class with
 * {@link RootConfiguration} annotation, and uses it as the root application
 * context.
 * </p>
 * If multiple classes are found, the (optional) first argument to the
 * application can be used to qualify by name.
 * 
 * Automatically finds the (exactly 1) {@link CommandLine} annotated class and set this as the commandline block
 * @author Daniel Burrell
 *
 */
public class SpringAutoJavaConfigCmdApplication {
    private static final Logger logger = LoggerFactory.getLogger(SpringAutoJavaConfigCmdApplication.class);

    public void run(String rootConfigName) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        boolean started = false;
        try {
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(RootConfiguration.class));
            Set<BeanDefinition> bd = scanner.findCandidateComponents("");

            if (bd.size() == 1) {
                logger.info("RootConfiguration found");
                String value = bd.iterator().next().getBeanClassName();
                started = executeCommandLine(context,value);
            } else if (bd.size() > 1) {
                logger.info("Multiple RootConfigurations found");
                if (StringUtils.isEmpty(rootConfigName)) {
                    throw new RuntimeException("Multiple RootConfigurations found, but no qualifier specified");
                }
                boolean found = false;
                while (bd.iterator().hasNext()) {
                    String value = bd.iterator().next().getBeanClassName();
                    RootConfiguration r = Class.forName(value).getAnnotation(RootConfiguration.class);
                    if (rootConfigName.equals(r.name())) {
                        found = true;
                        started = executeCommandLine(context,value);
                        break;
                    }
                }
                if (!found) {
                    throw new RuntimeException("Multiple RootConfigurations found, but none match the name: " + rootConfigName);
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

    private boolean executeCommandLine(AnnotationConfigApplicationContext context, String rootContext) throws IOException, ClassNotFoundException {
        context.register(Class.forName(rootContext));
        context.refresh();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(CommandLine.class));
        Set<BeanDefinition> bd = scanner.findCandidateComponents("");
        if (bd.size() == 1) {
            logger.info("CommandLine class found");
            String value = bd.iterator().next().getBeanClassName();
            Class<?> aClass = Class.forName(value);
            CommandLine r = Class.forName(value).getAnnotation(CommandLine.class);
            Object cmdl = context.getBean(aClass);
            logger.info("Starting application");
            ShellFactory.createConsoleShell(r.prompt(), "", cmdl).commandLoop();
        } else if (bd.size() == 0) {
            throw new RuntimeException("Expected exactly 1 class annotated with CommandLine to be present on the classpath.");
        } else if (bd.size() == 0) {
            throw new RuntimeException("Multiple CommandLine annotated classes found. Expected exactly 1");
        }

        context.close();
        return true;
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            new SpringAutoJavaConfigCmdApplication().run(args[0]);
        } else {
            new SpringAutoJavaConfigCmdApplication().run("");
        }
    }

}
