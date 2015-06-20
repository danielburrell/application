package uk.co.solong.application.main.java;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import uk.co.solong.application.annotations.CommandLine;
import asg.cliche.ShellFactory;

public class JavaCommandLineApplication {

    private static final Logger logger = LoggerFactory.getLogger(JavaCommandLineApplication.class);

    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(CommandLine.class));
        Set<BeanDefinition> bd = scanner.findCandidateComponents("");
        if (bd.size() == 1) {
            logger.info("CommandLine class found");
            String value = bd.iterator().next().getBeanClassName();
            Class<?> aClass = Class.forName(value);
            CommandLine r = Class.forName(value).getAnnotation(CommandLine.class);
            ShellFactory.createConsoleShell(r.prompt(), "", aClass.newInstance()).commandLoop();
        } else if (bd.size() == 0) {
            throw new RuntimeException("Expected at least 1 class annotated with CommandLine to be present on the classpath.");
        } else {
            if (args.length == 0) {
                throw new RuntimeException("Multiple CommandLine classes found, but no qualifier specified.");
            }
            String commandLineClass = args[0];

            logger.info("Multiple CommandLine classes found");
            if (StringUtils.isEmpty(commandLineClass)) {
                throw new RuntimeException("Multiple RootConfigurations found, but no qualifier specified.");
            }
            boolean found = false;
            while (bd.iterator().hasNext()) {
                String value = bd.iterator().next().getBeanClassName();
                CommandLine r = Class.forName(value).getAnnotation(CommandLine.class);
                if (commandLineClass.equals(r.name())) {
                    found = true;
                    Class<?> aClass = Class.forName(value);
                    ShellFactory.createConsoleShell(r.prompt(), "", aClass.newInstance()).commandLoop();
                    break;
                }
            }
            if (!found) {
                throw new RuntimeException("Multiple RootConfigurations found, but none match the name: " + commandLineClass);
            }
        }

    }
}
