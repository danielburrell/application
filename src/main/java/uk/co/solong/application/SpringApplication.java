package uk.co.solong.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

public final class SpringApplication {
	private static final Logger logger = LoggerFactory
			.getLogger(SpringApplication.class);

	public void run(String... configLocations) {

		if (configLocations == null || configLocations.length < 1) {
			throw new IllegalArgumentException("Config location missing");
		}

		GenericApplicationContext context = new GenericApplicationContext();
		BeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
		reader.loadBeanDefinitions(configLocations);

		boolean started = false;
		try {
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
		new SpringApplication().run(args);
	}

}