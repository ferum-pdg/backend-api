package org.heigvd.training_generator;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Configuration class for training generator version management.
 * This class manages the global version used across all training generation components.
 *
 * @version 2.0
 */
@ApplicationScoped
public class GeneratorConfiguration {

    /**
     * The current global version for all training generation components
     */
    private static final String GLOBAL_VERSION = "V2";

    /**
     * Retrieves the current global version for training generation.
     * This version determines which implementation classes will be used
     * throughout the training generation system.
     *
     * @return the current global version identifier
     */
    public String getGlobalVersion() {
        return GLOBAL_VERSION;
    }
}