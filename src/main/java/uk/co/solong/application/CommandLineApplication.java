package uk.co.solong.application;

import java.io.IOException;

import asg.cliche.ShellFactory;

public class CommandLineApplication {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String configClass = args[0];
        ClassLoader classLoader = CommandLineApplication.class.getClassLoader();
        Class<?> aClass = classLoader.loadClass(configClass);
        ShellFactory.createConsoleShell("hello", "", aClass.newInstance()).commandLoop(); 
    }
}
