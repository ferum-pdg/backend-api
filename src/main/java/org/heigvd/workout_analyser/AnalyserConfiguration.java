package org.heigvd.workout_analyser;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Configuration class for workout analyzer version management.
 * This class manages the global version used across all workout analysis components.
 *
 * Currently configured to use version V1 of the workout analyzer implementation.
 * Future versions can be activated by updating the GLOBAL_VERSION constant.
 *
 * @version 1.0
 */
@ApplicationScoped
public class AnalyserConfiguration {

    /**
     * The current global version for all workout analysis components
     */
    private static final String GLOBAL_VERSION = "V1";

    /**
     * Retrieves the current global version for workout analysis.
     * This version determines which analyzer implementation will be used
     * throughout the workout analysis system.
     *
     * @return the current global version identifier
     */
    public String getGlobalVersion() {
        return GLOBAL_VERSION;
    }
}