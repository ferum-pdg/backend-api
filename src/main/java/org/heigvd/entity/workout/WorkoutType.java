package org.heigvd.entity.workout;

import static org.heigvd.entity.workout.IntensityZone.*;

public enum WorkoutType {
    EF(RECOVERY, ENDURANCE, "Endurance Fondamentale", "Développer la base aérobie, soutenir un effort long."),
    EA(TEMPO, "Endurance active", "Améliorer la capacité à maintenir un effort soutenu."),
    LACTATE(THRESHOLD, "Lactate", "Augmenter la tolérance à un effort prolongé intense (~1h)."),
    INTERVAL(VO2_MAX, "Intervalles", "Améliorer la capacité aérobie maximale par des efforts courts et intenses."),
    TECHNIC(RECOVERY, "Technique", "Améliorer la technique, la posture et l'efficacité."),
    RA(RECOVERY, "Récupération active", "Permettre la récupération active après un effort intense.");


    private final IntensityZone minIntensityZone;
    private final IntensityZone maxIntensityZone;
    private final String label;
    private final String goal;

    WorkoutType(IntensityZone minIntensityZone, IntensityZone maxIntensityZone, String label, String goal) {
        this.minIntensityZone = minIntensityZone;
        this.maxIntensityZone = maxIntensityZone;
        this.label = label;
        this.goal = goal;
    }

    WorkoutType(IntensityZone intensityZone, String label, String goal) {
        this(intensityZone, intensityZone, label, goal);
    }

    public IntensityZone getMinIntensityZone() { return minIntensityZone; }
    public IntensityZone getMaxIntensityZone() { return maxIntensityZone; }
    public String getLabel() { return label; }
    public String getGoal() { return goal; }
}
