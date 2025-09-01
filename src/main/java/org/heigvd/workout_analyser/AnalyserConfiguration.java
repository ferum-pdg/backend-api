package org.heigvd.workout_analyser;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AnalyserConfiguration {

    private static final String GLOBAL_VERSION = "V1";

    public String getGlobalVersion() { return GLOBAL_VERSION; }
}
