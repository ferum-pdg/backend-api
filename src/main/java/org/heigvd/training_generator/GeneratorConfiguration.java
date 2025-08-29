package org.heigvd.training_generator;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GeneratorConfiguration {

    private static final String GLOBAL_VERSION = "V2";

    public String getGlobalVersion() {
        return GLOBAL_VERSION;
    }
}