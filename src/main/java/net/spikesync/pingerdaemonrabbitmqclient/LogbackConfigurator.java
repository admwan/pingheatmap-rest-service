package net.spikesync.pingerdaemonrabbitmqclient;

import java.io.InputStream;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;


public class LogbackConfigurator {
    private static final org.slf4j.Logger LOG =
            LoggerFactory.getLogger(LogbackConfigurator.class);
    
    public static boolean configure(String resource) throws JoranException {
        final InputStream configInputStream = LogbackConfigurator.class.getResourceAsStream(resource);
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        //  the context was probably already configured by default configuration rules
        loggerContext.reset();
        
        if(configInputStream != null) {
            try {
                configurator.doConfigure(configInputStream);
            } catch (JoranException e) {
                e.printStackTrace();
            }
            StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
            return true;
        } else {
            LOG.error("Unable to find logback file: {}", resource);
            StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
            return false;
        }
    }
}