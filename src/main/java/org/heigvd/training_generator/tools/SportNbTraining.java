package org.heigvd.training_generator.tools;

import org.heigvd.entity.Sport;

/**
 * Data transfer object for sport-specific training volume information.
 * This class encapsulates the relationship between a sport and the number
 * of training sessions allocated to it within a training plan.
 *
 * Used primarily for workout distribution calculations and sport-specific
 * volume management in training plan generation.
 *
 * @version 1.0
 */
public class SportNbTraining {

    /**
     * The sport for this training allocation
     */
    public Sport sport;

    /**
     * The number of training sessions for this sport
     */
    public int nbTraining;

    /**
     * Creates a new sport training allocation.
     *
     * @param sport the sport for this allocation
     * @param nbTraining the number of training sessions
     */
    public SportNbTraining(Sport sport, int nbTraining) {
        this.sport = sport;
        this.nbTraining = nbTraining;
    }
}