package com.demo.command;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

/**
 * Main application class for the Command Service.
 * This service handles all write operations in the CQRS pattern.
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.demo.feign.client")
@EnableKafka
public class CommandServiceApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CommandServiceApplication.class);
        app.addInitializers(new FactoryBeanAttributeFixerInitializer());
        app.run(args);
    }

    /**
     * ApplicationContextInitializer that runs early to fix factoryBeanObjectType issues
     */
    public static class FactoryBeanAttributeFixerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            // Add a BeanDefinitionRegistryPostProcessor that runs before BeanFactoryPostProcessor
            applicationContext.addBeanFactoryPostProcessor(new BeanDefinitionRegistryPostProcessor() {
                @Override
                public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
                    System.out.println("[FactoryBeanAttributeFixer] Scanning for factoryBeanObjectType attributes...");
                    String[] beanNames = registry.getBeanDefinitionNames();
                    for (String beanName : beanNames) {
                        try {
                            var bd = registry.getBeanDefinition(beanName);
                            Object attr = bd.getAttribute("factoryBeanObjectType");
                            if (attr instanceof String) {
                                String className = (String) attr;
                                System.out.println("[FactoryBeanAttributeFixer] Found problematic bean: " + beanName + " with class: " + className);
                                try {
                                    Class<?> clazz = Class.forName(className);
                                    bd.setAttribute("factoryBeanObjectType", clazz);
                                    System.out.println("[FactoryBeanAttributeFixer] Fixed factoryBeanObjectType for bean: " + beanName);
                                } catch (ClassNotFoundException e) {
                                    System.err.println("[FactoryBeanAttributeFixer] Could not load class " + className + " for bean " + beanName + ": " + e.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            // Ignore errors during attribute fixing
                        }
                    }
                }

                @Override
                public void postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory beanFactory) {
                    // Additional processing if needed
                }
            });

            // Also add the BeanFactoryPostProcessor as a backup
            applicationContext.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {
                @Override
                public void postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory beanFactory) {
                    if (beanFactory instanceof BeanDefinitionRegistry registry) {
                        String[] beanNames = registry.getBeanDefinitionNames();
                        for (String beanName : beanNames) {
                            try {
                                var bd = registry.getBeanDefinition(beanName);
                                Object attr = bd.getAttribute("factoryBeanObjectType");
                                if (attr instanceof String) {
                                    String className = (String) attr;
                                    try {
                                        Class<?> clazz = Class.forName(className);
                                        bd.setAttribute("factoryBeanObjectType", clazz);
                                        System.out.println("[FactoryBeanAttributeFixer] Fixed factoryBeanObjectType for bean: " + beanName + " -> " + className);
                                    } catch (ClassNotFoundException e) {
                                        System.err.println("[FactoryBeanAttributeFixer] Could not load class " + className + " for bean " + beanName + ": " + e.getMessage());
                                    }
                                }
                            } catch (Exception e) {
                                // Ignore errors during attribute fixing
                            }
                        }
                    }
                }
            });
        }
    }
}