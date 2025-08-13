package org.heigvd.entity;

public enum ElevationType {
    LOW,
    MEDIUM,
    HIGH;

    // write a function that return an ElevationType based on a given elevation gain
    public static ElevationType getElevationType(double gain, Sport sport) {
        if (sport == Sport.CYCLING) {
            if (gain < 500) {
                return LOW;
            } else if (gain < 1500) {
                return MEDIUM;
            } else {
                return HIGH;
            }
        } else if (sport == Sport.RUNNING) {
            if (gain < 200) {
                return LOW;
            } else if (gain < 800) {
                return MEDIUM;
            } else {
                return HIGH;
            }
        } else {
            throw new IllegalArgumentException("Unsupported sport type: " + sport);
        }
    }
}
