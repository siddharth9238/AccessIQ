package com.accessiq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for AccessIQ.
 */
@SpringBootApplication
@EnableScheduling
public class AccessiqApplication {

    private static final Logger log = LoggerFactory.getLogger(AccessiqApplication.class);

    /**
     * Main entry point for the application.
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(AccessiqApplication.class, args);
        log.info("AccessIQ Application started successfully");
    }
}